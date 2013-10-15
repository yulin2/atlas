package org.atlasapi.remotesite.bbc.nitro;

import com.metabroadcast.common.scheduling.UpdateProgress;


public interface ChannelDayProcessingTaskListener {

    void channelDayCompleted(ChannelDay channelDay, UpdateProgress progress);
    void completed(UpdateProgress progress);
    
}
