package org.atlasapi.remotesite.metabroadcast.picks;

import static com.google.common.collect.Iterables.*;

import java.util.List;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.remotesite.bbc.nitro.ChannelDay;
import org.atlasapi.remotesite.bbc.nitro.ChannelDayProcessor;
import org.joda.time.DateTimeZone;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.UpdateProgress;


public class PicksDayUpdater implements ChannelDayProcessor {

    private static final String CONTENT_GROUP = "http://picks.metabroadcast.com/schedule-picks";
    private final ScheduleResolver scheduleResolver;
    private final ContentGroupResolver contentGroupResolver;
    private final ContentGroupWriter contentGroupWriter;
    private final PicksChannelsSupplier picksChannelsSupplier;
    
    public PicksDayUpdater(ScheduleResolver scheduleResolver, ContentGroupResolver contentGroupResolver,
            ContentGroupWriter contentGroupWriter, PicksChannelsSupplier picksChannelsSupplier) {
        this.scheduleResolver = scheduleResolver;
        this.contentGroupResolver = contentGroupResolver;
        this.contentGroupWriter = contentGroupWriter;
        this.picksChannelsSupplier = picksChannelsSupplier;
    }
    
    @Override
    public UpdateProgress process(ChannelDay channelDay) throws Exception {
        try {
            Iterable<Item> picks = findPicks(scheduleResolver.schedule(
                    channelDay.getDay().toDateTimeAtStartOfDay(DateTimeZone.UTC), 
                    channelDay.getDay().plusDays(1).toDateTimeAtStartOfDay(DateTimeZone.UTC), 
                    ImmutableSet.of(channelDay.getChannel()), 
                    ImmutableSet.of(Publisher.PA), Optional.<ApplicationConfiguration>absent()));
            
            addPicksToContentGroup(picks);
            
            return UpdateProgress.SUCCESS;
        } catch (Exception e) {
            return UpdateProgress.FAILURE;
        }
    }

    private Iterable<Item> findPicks(Schedule schedule) {
        return filter(concat(transform(schedule.scheduleChannels(), TO_ITEMS)), 
                new PickPredicate(picksChannelsSupplier.get()));
    }
    
    private void addPicksToContentGroup(Iterable<Item> items) {
        ContentGroup contentGroup = resolveOrCreateContentGroup();
        Iterable<ChildRef> childRefs = transform(items, Item.TO_CHILD_REF);
        for (ChildRef childRef : childRefs) {
            if (!contentGroup.getContents().contains(childRef)) {
                contentGroup.addContent(childRef);
            }
        }
        contentGroupWriter.createOrUpdate(contentGroup);
    }
    
    private ContentGroup resolveOrCreateContentGroup() {
        ResolvedContent contentGroup = contentGroupResolver.findByCanonicalUris(ImmutableSet.of(CONTENT_GROUP));
        Maybe<Identified> first = contentGroup.getFirstValue();
        
        if (first.hasValue()) {
            return (ContentGroup) first.requireValue();
        }
        
        return new ContentGroup(CONTENT_GROUP, Publisher.METABROADCAST_PICKS);
    }
    
    private static final Function<ScheduleChannel, List<Item>> TO_ITEMS = new Function<ScheduleChannel, List<Item>>() {

        @Override
        public List<Item> apply(ScheduleChannel scheduleChannel) {
            return scheduleChannel.items();
        }
    };
}
