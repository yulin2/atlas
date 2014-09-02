package org.atlasapi.remotesite.metabroadcast.picks;

import org.atlasapi.remotesite.bbc.nitro.ChannelDay;
import org.atlasapi.remotesite.bbc.nitro.ChannelDayProcessingTaskListener;
import org.joda.time.LocalDate;

import com.metabroadcast.common.scheduling.UpdateProgress;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;


public class PicksScheduledTaskListener implements ChannelDayProcessingTaskListener {

    private LocalDate lastDayCompleted;
    private final PicksLastProcessedStore picksLastProcessedStore;
    private final Clock clock;
    
    public PicksScheduledTaskListener(PicksLastProcessedStore picksLastProcessedStore) {
        this.picksLastProcessedStore = picksLastProcessedStore;
        this.clock = new SystemClock();
    }
    
    @Override
    public void channelDayCompleted(ChannelDay channelDay, UpdateProgress progress) {
        // Only mark up until today as completed, as we'll re-run future days in case of
        // schedule changes
        if ((lastDayCompleted == null || channelDay.getDay().isAfter(lastDayCompleted))
                        && channelDay.getDay().isBefore(clock.now().toLocalDate())) {
            lastDayCompleted = channelDay.getDay();
        }
    }

    @Override
    public void completed(UpdateProgress progress) {
        if (!progress.hasFailures() 
                && lastDayCompleted != null) {
            picksLastProcessedStore.setLastScheduleDayProcessed(lastDayCompleted);
        }
    }

}
