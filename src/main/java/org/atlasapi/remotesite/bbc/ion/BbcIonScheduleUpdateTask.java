package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.List;
import java.util.concurrent.Callable;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class BbcIonScheduleUpdateTask implements Callable<Integer> {

    private final String scheduleUrl;
    private final RemoteSiteClient<IonSchedule> scheduleClient;
    private final BbcIonBroadcastHandler handler;
    private final AdapterLog log;
    private final BroadcastTrimmer trimmer;
    private final ChannelResolver channelResolver;
    private final ScheduleWriter scheduleWriter;

    public BbcIonScheduleUpdateTask(String scheduleUrl, RemoteSiteClient<IonSchedule> scheduleClient, BbcIonBroadcastHandler handler, BroadcastTrimmer trimmer, ChannelResolver channelResolver, ScheduleWriter scheduleWriter, AdapterLog log) {
        this.scheduleUrl = scheduleUrl;
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.trimmer = trimmer;
        this.channelResolver = channelResolver;
        this.scheduleWriter = scheduleWriter;
        this.log = log;
    }

    @Override
    public Integer call() throws Exception {
        log.record(debugEntry().withSource(getClass()).withDescription("update schedule: %s", scheduleUrl));

        try {
            IonSchedule schedule = scheduleClient.get(scheduleUrl);
            List<ItemAndBroadcast> itemAndBroadcasts = Lists.newArrayList();
            for (IonBroadcast broadcast : schedule.getBlocklist()) {
                Maybe<ItemAndBroadcast> itemAndBroadcast = handler.handle(broadcast);
                if(itemAndBroadcast.hasValue() && itemAndBroadcast.requireValue().getBroadcast().hasValue()) {
                    itemAndBroadcasts.add(itemAndBroadcast.requireValue());
                }
            }
            trimBroadcastsAndUpdateSchedule(itemAndBroadcasts);
            return schedule.getBlocklist().size();
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("exception handling schedule schedule: %s", scheduleUrl));
            throw e;
        }
    }

    private void trimBroadcastsAndUpdateSchedule(List<ItemAndBroadcast> itemAndBroadcasts) {
        DateTime scheduleStartTime = null;
        DateTime scheduleEndTime = null;
        String broadcastOn = null;
        
        if(!itemAndBroadcasts.isEmpty()) {
            Builder<String, String> acceptableIds = ImmutableMap.builder();
            for(ItemAndBroadcast itemAndBroadcast : itemAndBroadcasts) {
                if(itemAndBroadcast.getBroadcast().hasValue()) {
                    Broadcast broadcast = itemAndBroadcast.getBroadcast().requireValue();
                    
                    if(broadcastOn != null && !broadcastOn.equals(broadcast.getBroadcastOn())) {
                        throw new IllegalStateException("Not expecting broadcasts on multiple channels from a single schedule URL");
                    }
                    
                    broadcastOn = broadcast.getBroadcastOn();
                    
                    if(scheduleStartTime == null || scheduleStartTime.isAfter(broadcast.getTransmissionTime())) {
                        scheduleStartTime = broadcast.getTransmissionTime();
                    }
                    if(scheduleEndTime == null || scheduleEndTime.isBefore(broadcast.getTransmissionEndTime())) {
                        scheduleEndTime = broadcast.getTransmissionEndTime();
                    }
                    acceptableIds.put(broadcast.getSourceId(), itemAndBroadcast.getItem().getCanonicalUri());
                }
            }
            
            Channel channel = channelResolver.fromUri(broadcastOn).requireValue();
            trimmer.trimBroadcasts(new Interval(scheduleStartTime, scheduleEndTime), channel, acceptableIds.build());
            scheduleWriter.replaceScheduleBlock(Publisher.BBC, channel, Iterables.transform(itemAndBroadcasts, TO_ITEM_REF_AND_BROADCAST));
        }
    }
    
    private static final Function<ItemAndBroadcast, ItemRefAndBroadcast> TO_ITEM_REF_AND_BROADCAST = new Function<ItemAndBroadcast, ItemRefAndBroadcast>() {

        @Override
        public ItemRefAndBroadcast apply(ItemAndBroadcast input) {
            return new ItemRefAndBroadcast(input.getItem(), input.getBroadcast().requireValue());
        }
        
    };
}
