package org.atlasapi.equiv;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.stats.Counter;

public class BrandEquivUpdateTask implements Runnable {

    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4);
    private static final Duration BROADCAST_FLEXIBILITY = Duration.standardMinutes(1);
    
    private final Brand brand;
    private final ScheduleResolver scheduleResolver;
    private final ContentWriter contentWriter;
    private final AdapterLog log;

    private double certaintyThreshold = 0.9;

    public BrandEquivUpdateTask(Brand brand, ScheduleResolver scheduleResolver, ContentWriter contentWriter, AdapterLog log) {
        this.brand = brand;
        this.scheduleResolver = scheduleResolver;
        this.contentWriter = contentWriter;
        this.log = log;
    }

    @Override
    public void run() {
        updateEquivalence(brand);
    }

    private void updateEquivalence(Brand brand) {
        
        List<Brand> equivBrands = Lists.newArrayList();
        
        Multimap<Publisher, Brand> suggestedBrands = ArrayListMultimap.create();
        
        for (Item item : brand.getContents()) {
            suggestedBrands.putAll(suggestedBrandsForItem(item));
        }

        for (Publisher publisher : TARGET_PUBLISHERS) {
            Brand mostSuggested = mostSuggested(suggestedBrands.get(publisher));
            if (mostSuggested != null) {
                equivalise(brand, mostSuggested);
                equivBrands.add(mostSuggested);
            }
        }
        
        if(!equivBrands.isEmpty()) {
            contentWriter.createOrUpdate(brand, true);
            for (Brand equivBrand : equivBrands) {
                contentWriter.createOrUpdate(equivBrand, true);
            }
            log.record(debugEntry().withSource(getClass()).withDescription(String.format("Equivalised %s to %s others", brand.getCanonicalUri(), equivBrands.size())));
        }
    }

    private Multimap<Publisher, Brand> suggestedBrandsForItem(Item item) {
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
        
        Map<Publisher, Brand> suggestedBrands = Maps.newHashMap();
                
        for (Publisher publisher : TARGET_PUBLISHERS) {
            Episode mostSuggested = mostSuggested(binnedSuggestedItems.get(publisher));
            if(mostSuggested != null && mostSuggested.getFullContainer() instanceof Brand) {
                suggestedBrands.put(publisher, (Brand) mostSuggested.getFullContainer());
            }
        }

        return Multimaps.forMap(suggestedBrands);
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

    private <T extends Identified> T mostSuggested(Collection<T> suggestedItems) {
        if(!suggestedItems.isEmpty()) {
            List<Count<T>> potItemCounts = count(suggestedItems);
            Count<T> mostSuggestedCount = Ordering.natural().greatestOf(potItemCounts, 1).get(0);
            if(mostSuggestedCount.getCount() / Double.valueOf(suggestedItems.size()) > certaintyThreshold) {
                return mostSuggestedCount.getTarget();
            }
        }
        return null;
    }

    private void equivalise(Brand subjectBrand, Brand mostSuggested) {
        subjectBrand.addEquivalentTo(mostSuggested);
        mostSuggested.addEquivalentTo(subjectBrand);
    }

    private <T extends Identified> List<Count<T>> count(Iterable<T> potEquivItems) {
        Counter<T, T> counter = new Counter<T, T>();
        for (T item : potEquivItems) {
            counter.incrementCount(item, item);
        }
        return counter.counts(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getCanonicalUri().compareTo(o2.getCanonicalUri());
            }
        });
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
}
