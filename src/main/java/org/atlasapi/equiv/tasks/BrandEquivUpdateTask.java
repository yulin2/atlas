package org.atlasapi.equiv.tasks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.stats.Counter;

public class BrandEquivUpdateTask implements Callable<EquivResult> {

    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4);
    private static final Duration BROADCAST_FLEXIBILITY = Duration.standardMinutes(1);
    
    private final Brand brand;
    private final ScheduleResolver scheduleResolver;
    private final ContentWriter contentWriter;
//    private final AdapterLog log;

    private double certaintyThreshold = 0.9;
    private boolean writesResults = true;

    public BrandEquivUpdateTask(Brand brand, ScheduleResolver scheduleResolver, ContentWriter contentWriter, AdapterLog log) {
        this.brand = brand;
        this.scheduleResolver = scheduleResolver;
        this.contentWriter = contentWriter;
//        this.log = log;
    }

    @Override
    public EquivResult call() {
        return updateEquivalence(brand);
    }

    private EquivResult updateEquivalence(Brand brand) {
        
        List<Brand> equivBrands = Lists.newArrayList();
        
        Multimap<Publisher, Brand> suggestedBrands = ArrayListMultimap.create();
        
        List<EquivResult> episodeEquivResults = Lists.newArrayListWithExpectedSize(brand.getContents().size());
        for (Episode episode : brand.getContents()) {
            Multimap<Publisher, Episode> suggestedEquivalents = suggestedEquivalents(episode);
            Map<Publisher, List<Count<Episode>>> countedEquivalentItems = countedSuggestedEquivalents(suggestedEquivalents);
            
            episodeEquivResults.add(EquivResult.of(episode, Ordering.natural().reverse().immutableSortedCopy(Iterables.concat(countedEquivalentItems.values()))));
            suggestedBrands.putAll(Multimaps.forMap(getBrands(mostSuggestedPerPublisher(countedEquivalentItems))));
        }

        Map<Publisher, List<Count<Brand>>> countedEquivalentBrands = countedSuggestedEquivalents(suggestedBrands);
        
        Map<Publisher, Brand> mostSuggestedBrandPerPublisher = mostSuggestedPerPublisher(countedEquivalentBrands);
        
        for (Publisher publisher : TARGET_PUBLISHERS) {
            Brand mostSuggested = mostSuggestedBrandPerPublisher.get(publisher);
            if (mostSuggested != null) {
                equivalise(brand, mostSuggested);
                equivBrands.add(mostSuggested);
            }
        }
        
        if(!equivBrands.isEmpty() && writesResults) {
            contentWriter.createOrUpdate(brand, false);
            for (Brand equivBrand : equivBrands) {
                contentWriter.createOrUpdate(equivBrand, false);
            }
        }
        
        return EquivResult.of(brand, Ordering.natural().reverse().immutableSortedCopy(Iterables.concat(countedEquivalentBrands.values()))).withSubResults(episodeEquivResults);
    }

    private Map<Publisher, Brand> getBrands(Map<Publisher, Episode> mostSuggestedPerPublisher) {
        return Maps.filterValues(Maps.transformValues(mostSuggestedPerPublisher, new Function<Episode, Brand>() {
            @Override
            public Brand apply(Episode input) {
                if(input.getFullContainer() instanceof Brand) {
                    return (Brand) input.getFullContainer();
                }
                return null;
            }
        }), Predicates.notNull());
    }

    private <T extends Identified> Map<Publisher, T> mostSuggestedPerPublisher(Map<Publisher, List<Count<T>>> suggestedEquivalentItems) {
        return Maps.filterValues(Maps.transformValues(suggestedEquivalentItems, new Function<List<Count<T>>, T>() {
            @Override
            public T apply(List<Count<T>> input) {
                Count<T> mostSuggestedCount = input.get(0);
                
                if(mostSuggestedCount.getCount() / Double.valueOf(sum(input)) > certaintyThreshold) {
                    return mostSuggestedCount.getTarget();
                }
                return null;
            }
        }), Predicates.notNull());
    }
    
    private <T> long sum(Iterable<Count<T>> counts) {
        long total = 0;
        for (Count<T> count : counts) {
            total += count.getCount();
        }
        return total;
    }
    
    private <T extends Identified> Map<Publisher, List<Count<T>>> countedSuggestedEquivalents(Multimap<Publisher, T> binnedSuggested) {
        Map<Publisher, List<Count<T>>> binnedSuggestedItemCounts = Maps.newHashMap();
        
        for (Publisher publisher : TARGET_PUBLISHERS) {
            if(!binnedSuggested.get(publisher).isEmpty()) {
                binnedSuggestedItemCounts.put(publisher, orderedCounts(binnedSuggested.get(publisher)));
            }
        }
        
        return binnedSuggestedItemCounts;
    }

    private Multimap<Publisher, Episode> suggestedEquivalents(Item item) {
        Multimap<Publisher, Episode> binnedSuggestedItems = ArrayListMultimap.create();
        
        for (Version version : item.getVersions()) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                
                Schedule schedule = scheduleAround(broadcast);
                for (ScheduleChannel channel : schedule.scheduleChannels()) {
                    for (Item scheduleItem : channel.items()) {
                        if(scheduleItem instanceof Episode && hasQualifyingBroadcast(scheduleItem, broadcast)) {
                            binnedSuggestedItems.put(scheduleItem.getPublisher(), (Episode) scheduleItem);
                        }
                    }
                }
                
            }
        }
        return binnedSuggestedItems;
    }

    private boolean hasQualifyingBroadcast(Item item, Broadcast referenceBroadcast) {
        for (Version version : item.getVersions()) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                if(around(broadcast, referenceBroadcast) && broadcast.getBroadcastOn() != null &&  broadcast.getBroadcastOn().equals(referenceBroadcast.getBroadcastOn())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean around(Broadcast broadcast, Broadcast referenceBroadcast) {
        return around(broadcast.getTransmissionTime(), referenceBroadcast.getTransmissionTime())
            && around(broadcast.getTransmissionEndTime(), referenceBroadcast.getTransmissionEndTime());
    }

    private boolean around(DateTime transmissionTime, DateTime transmissionTime2) {
        return transmissionTime.isAfter(transmissionTime2.minus(BROADCAST_FLEXIBILITY)) && transmissionTime.isBefore(transmissionTime2.plus(BROADCAST_FLEXIBILITY));
    }

    private void equivalise(Brand subjectBrand, Brand mostSuggested) {
        subjectBrand.addEquivalentTo(mostSuggested);
        mostSuggested.addEquivalentTo(subjectBrand);
    }

    private <T extends Identified> List<Count<T>> orderedCounts(Iterable<T> potEquivItems) {
        Counter<T, T> counter = new Counter<T, T>();
        for (T item : potEquivItems) {
            counter.incrementCount(item, item);
        }
        return Ordering.natural().reverse().immutableSortedCopy(counter.counts(Ordering.usingToString()));
    }
    
    private Schedule scheduleAround(Broadcast broadcast) {
        DateTime start = broadcast.getTransmissionTime().minus(BROADCAST_FLEXIBILITY);
        DateTime end = broadcast.getTransmissionEndTime().plus(BROADCAST_FLEXIBILITY);
        Channel channel = Channel.fromUri(broadcast.getBroadcastOn()).requireValue();
        
        return scheduleResolver.schedule(start, end, ImmutableSet.of(channel), TARGET_PUBLISHERS);
    }
    
    public BrandEquivUpdateTask withCertaintyThreshold(double threshold) {
        this.certaintyThreshold = threshold;
        return this;
    }
    
    public BrandEquivUpdateTask writesResults(boolean shouldWriteResults) {
        this.writesResults = shouldWriteResults;
        return this;
    }
}
