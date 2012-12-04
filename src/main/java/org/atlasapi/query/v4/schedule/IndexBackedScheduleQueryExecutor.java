package org.atlasapi.query.v4.schedule;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.EquivalentContent;
import org.atlasapi.persistence.content.EquivalentContentResolver;
import org.atlasapi.persistence.content.schedule.ScheduleIndex;
import org.atlasapi.persistence.content.schedule.ScheduleRef;
import org.atlasapi.persistence.content.schedule.ScheduleRef.ScheduleRefEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class IndexBackedScheduleQueryExecutor implements ScheduleQueryExecutor{

    private static final long QUERY_TIMEOUT = 60000;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private EquivalentContentResolver contentQueryExecutor;
    private ScheduleIndex index;

    public IndexBackedScheduleQueryExecutor(ScheduleIndex index, EquivalentContentResolver contentQueryExecutor) {
        this.index = index;
        this.contentQueryExecutor = contentQueryExecutor;
    }

    @Override
    public ScheduleQueryResult execute(ScheduleQuery query) throws ScheduleQueryExecutionException {
        long id = Thread.currentThread().getId();
        long startIndexTime = System.currentTimeMillis();

        ListenableFuture<ScheduleRef> futureRef = queryIndex(query);
        ScheduleRef scheduleRef = Futures.get(futureRef, QUERY_TIMEOUT, MILLISECONDS, ScheduleQueryExecutionException.class);
        long indexTime = System.currentTimeMillis() - startIndexTime;

        long startRetrieveTime = System.currentTimeMillis();
        ChannelSchedule schedule = transformToChannelSchedule(query, scheduleRef);
        long retrieveTime = System.currentTimeMillis() - startRetrieveTime;

        log.info("{}: schedule times: {}\t{}", new Object[]{id, indexTime, retrieveTime});
        return new ScheduleQueryResult(schedule, query.getAnnotations(), query.getApplicationConfiguration());
    }

    private ListenableFuture<ScheduleRef> queryIndex(ScheduleQuery query) {
        return index.resolveSchedule(query.getPublisher(), query.getChannel(), query.getInterval());
    }

    private ChannelSchedule transformToChannelSchedule(ScheduleQuery query, ScheduleRef scheduleRef) {
        if (scheduleRef.isEmpty()) {
            return new ChannelSchedule(query.getChannel(), query.getInterval(), ImmutableList.<Item>of());
        }
        
        Builder<String> uris = ImmutableList.builder();
        for (ScheduleRefEntry entry : scheduleRef.getScheduleEntries()) {
            uris.add(entry.getItemUri());
        }
        
        EquivalentContent equivalentContent = contentQueryExecutor.resolveUris(
            Iterables.transform(scheduleRef.getScheduleEntries(), new ScheduleRefEntryToUri()), 
            ImmutableSet.of(query.getPublisher()), 
            query.getAnnotations(), 
            false
        );
        
        return new ChannelSchedule(query.getChannel(), query.getInterval(), contentList(query, scheduleRef, equivalentContent.asMap()));
    }

    private Iterable<Item> contentList(ScheduleQuery query, ScheduleRef scheduleRef, Map<String, Collection<Content>> resolvedContent) {
        Builder<Item> contentList = ImmutableList.builder();
        for (ScheduleRefEntry entry : scheduleRef.getScheduleEntries()){
            Collection<Content> resolved = resolvedContent.get(entry.getItemUri());
            /* copy the item here because it may appear in the same schedule
             * twice but will only appear in resolvedContent once.
             */
            Item item = findRequestSourceItem(query, resolved);
            if (item != null) {
                item = item.copy();
                if (trimBroadcasts(entry, item)) {
                    contentList.add(item);
                }
            }
        }
        return contentList.build();
    }

    /* This handles when resolved content is either merged on un-merged. We 
     * always want the requested source's content so we can find the broadcast.
     */
    private Item findRequestSourceItem(ScheduleQuery query, Collection<Content> resolved) {
        if (resolved != null) {
            for (Identified identified : resolved) {
                if (identified instanceof Item) {
                    Item item = (Item) identified;
                    if (query.getPublisher().equals(item.getPublisher())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    private boolean trimBroadcasts(ScheduleRefEntry entry, Item item) {
        boolean found = false;
        for (Version version : item.getVersions()) {
            Set<Broadcast> allBroadcasts = version.getBroadcasts();
            version.setBroadcasts(Sets.<Broadcast>newHashSet());
            if (found) {
                continue;
            }
            for (Broadcast broadcast : allBroadcasts) {
                if (relevantBroadcast(broadcast, entry)) {
                    version.setBroadcasts(Sets.newHashSet(broadcast));
                    found = true;
                }
            }
        }
        return found;
    }

    private boolean relevantBroadcast(Broadcast broadcast, ScheduleRefEntry entry) {
        return broadcast.isActivelyPublished()
            && matchingChannel(broadcast, entry)
            && (matchingSourceId(broadcast, entry) || matchingStartTime(broadcast, entry));
    }

    private boolean matchingStartTime(Broadcast broadcast, ScheduleRefEntry entry) {
        return broadcast.getTransmissionTime().equals(entry.getBroadcastTime());
    }

    private boolean matchingSourceId(Broadcast broadcast, ScheduleRefEntry entry) {
        return broadcast.getSourceId() != null && broadcast.getSourceId().equals(entry.getBroadcastId().orNull());
    }

    private boolean matchingChannel(Broadcast broadcast, ScheduleRefEntry entry) {
        return broadcast.getBroadcastOn().equals(entry.getChannelId());
    }

    /* Because of the way that equivalence merging currently works we have to 
     * make sure that the requested publisher is precedent to get the 
     * referenced broadcast in the content (broadcasts on other versions are 
     * removed.) Yum. 
     */
    private ApplicationConfiguration ensureRequestedPublisherIsPrecedent(ScheduleQuery query, ApplicationConfiguration appConfig) {
        if (!appConfig.precedenceEnabled()) {
            return appConfig;
        }
        List<Publisher> publishers = Lists.newArrayList(query.getPublisher());
        for (Publisher publisher : appConfig.orderdPublishers()) {
            if (!publisher.equals(query.getPublisher())) {
                publishers.add(publisher);
            }
        }
        return appConfig.copyWithPrecedence(publishers);
    }
    
    private static class ScheduleRefEntryToUri implements Function<ScheduleRefEntry, String> {

        @Override
        public String apply(ScheduleRefEntry input) {
            return input.getItemUri();
        }
        
    }
}
