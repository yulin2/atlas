package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.List;
import java.util.concurrent.Callable;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class BbcIonScheduleUpdateTask implements Callable<Integer> {

    private final String scheduleUrl;
    private final RemoteSiteClient<IonSchedule> scheduleClient;
    private final BbcIonBroadcastHandler handler;
    private final AdapterLog log;
    private final ChannelResolver channelResolver;

    public BbcIonScheduleUpdateTask(String scheduleUrl, RemoteSiteClient<IonSchedule> scheduleClient, BbcIonBroadcastHandler handler, ChannelResolver channelResolver, AdapterLog log) {
        this.scheduleUrl = scheduleUrl;
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.channelResolver = channelResolver;
        this.log = log;
    }

    @Override
    public Integer call() throws Exception {
        log.record(debugEntry().withSource(getClass()).withDescription("update schedule: %s", scheduleUrl));

        try {
            IonSchedule schedule = scheduleClient.get(scheduleUrl);
            List<ItemAndPossibleBroadcast> itemAndBroadcasts = Lists.newArrayList();
            for (IonBroadcast broadcast : schedule.getBlocklist()) {
                Maybe<ItemAndPossibleBroadcast> itemAndBroadcast = handler.handle(broadcast);
                if(itemAndBroadcast.hasValue() && itemAndBroadcast.requireValue().getBroadcast().isPresent()) {
                    itemAndBroadcasts.add(itemAndBroadcast.requireValue());
                }
            }
            trimBroadcasts(itemAndBroadcasts);
            return schedule.getBlocklist().size();
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("exception handling schedule schedule: %s", scheduleUrl));
            throw e;
        }
    }

    private void trimBroadcasts(List<ItemAndPossibleBroadcast> itemAndBroadcasts) {
        DateTime scheduleStartTime = null;
        DateTime scheduleEndTime = null;
        String broadcastOn = null;
        
        if(!itemAndBroadcasts.isEmpty()) {
            Builder<String, String> acceptableIds = ImmutableMap.builder();
            for(ItemAndPossibleBroadcast itemAndBroadcast : itemAndBroadcasts) {
                if(itemAndBroadcast.getBroadcast().isPresent()) {
                    Broadcast broadcast = itemAndBroadcast.getBroadcast().get();
                    
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
        }
    }
}
