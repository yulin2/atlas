package org.atlasapi.remotesite.metabroadcast.picks;

import org.atlasapi.remotesite.bbc.nitro.ChannelDay;
import org.atlasapi.remotesite.bbc.nitro.ChannelDayProcessingTaskListener;
import org.joda.time.LocalDate;

import com.metabroadcast.common.scheduling.UpdateProgress;


public class PicksScheduledTaskListener implements ChannelDayProcessingTaskListener {

    private LocalDate lastDayCompleted;
    private final PicksLastProcessedStore picksLastProcessedStore;
    
    public PicksScheduledTaskListener(PicksLastProcessedStore picksLastProcessedStore) {
        this.picksLastProcessedStore = picksLastProcessedStore;
    }
    
    @Override
    public void channelDayCompleted(ChannelDay channelDay, UpdateProgress progress) {
        if (lastDayCompleted == null || channelDay.getDay().isAfter(lastDayCompleted)) {
            lastDayCompleted = channelDay.getDay();
        }
    }

    @Override
    public void completed(UpdateProgress progress) {
        if (!progress.hasFailures() && lastDayCompleted != null) {
            picksLastProcessedStore.setLastScheduleDayProcessed(lastDayCompleted);
        }
    }

}
