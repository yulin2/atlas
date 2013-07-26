package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
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
public class TalkTalkVodListsProcessingTask extends ScheduledTask {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TalkTalkClient client;
    private final TalkTalkChannelProcessor<List<Content>> channelProcessor;
    private final ContentGroupResolver resolver;
    private final ContentGroupWriter writer;
    private final TalkTalkVodListsProcessor<UpdateProgress> contentGroupProcessor;

    public TalkTalkVodListsProcessingTask(TalkTalkClient talkTalkClient,
            TalkTalkChannelProcessor<List<Content>> channelProcessor,
            ContentGroupResolver resolver,
            ContentGroupWriter writer,
            TalkTalkVodListsProcessor<UpdateProgress> contentGroupProcessor) {
        this.client = checkNotNull(talkTalkClient);
        this.channelProcessor = checkNotNull(channelProcessor);
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
        this.contentGroupProcessor = checkNotNull(contentGroupProcessor);
    }

    @Override
    protected void runTask() {
        try {
            client.processTvStructure(new TalkTalkTvStructureProcessor<UpdateProgress>() {

                private UpdateProgress progress = UpdateProgress.START;

                @Override
                public UpdateProgress getResult() {
                    return progress;
                }

                @Override
                public void process(ChannelType channel) {
                    try {
                        log.debug("processing channel {}", channel.getId());
                        List<Content> contentList = channelProcessor.process(channel);
                        progress = progress.reduce(contentGroupProcessor.process(resolver, writer, contentList, channel.getId()));
                        reportStatus(progress.toString());
                    } catch (TalkTalkException tte) {
                        log.error("failed to process " + channel.getId(), tte);
                        progress = progress.reduce(UpdateProgress.FAILURE);
                    }
                }

            });
        } catch (TalkTalkException tte) {
            log.error("content update failed", tte);
            // ensure task is marked failed
            throw new RuntimeException(tte);
        }
    }

}
