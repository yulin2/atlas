package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.talktalk.TalkTalkClient.TalkTalkTvStructureCallback;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * {@link ScheduledTask} which retrieves the TV Structure via the provided
 * {@link TalkTalkClient} and processes each {@link ChannelType} in turn using
 * the provided {@link TalkTalkChannelProcessor}.
 */
public class TalkTalkChannelProcessingTask extends ScheduledTask {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final TalkTalkClient client;

    private TalkTalkChannelProcessor<UpdateProgress> channelProcessor;

    public TalkTalkChannelProcessingTask(TalkTalkClient talkTalkClient, TalkTalkChannelProcessor<UpdateProgress> channelProcessor) {
        this.client = checkNotNull(talkTalkClient);
        this.channelProcessor = checkNotNull(channelProcessor);
    }

    @Override
    protected void runTask() {
        try {
            client.processTvStructure(new TalkTalkTvStructureCallback<UpdateProgress>() {

                private UpdateProgress progress = UpdateProgress.START;
                
                @Override
                public UpdateProgress getResult() {
                    return progress;
                }
                
                @Override
                public void process(ChannelType channel) {
                    try {
                        log.debug("processing channel {}", channel.getId());
                        progress = progress.reduce(channelProcessor.process(channel));
                        reportStatus(progress.toString());
                    } catch (TalkTalkException tte) {
                        log.error("failed to process " + channel.getId(), tte);
                        progress = progress.reduce(UpdateProgress.FAILURE);
                    }
                }
                
            });
        } catch (TalkTalkException tte) {
            // ensure task is marked failed, the exception is logged by the
            // scheduler.
            throw new RuntimeException(tte);
        }
    }
    
}
