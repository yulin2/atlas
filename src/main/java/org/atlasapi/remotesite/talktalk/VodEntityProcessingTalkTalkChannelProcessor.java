package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * TalkTalkChannelProcessor which fetches the VOD entity list for the channel
 * and processes each entity using the given TalkTalkContentEntityProcessor.
 */
public class VodEntityProcessingTalkTalkChannelProcessor implements
        TalkTalkChannelProcessor<UpdateProgress> {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final int _500_ITEMS_PER_PAGE = 500;
    
    private final TalkTalkClient client;
    private final TalkTalkContentEntityProcessor<UpdateProgress> processor;

    public VodEntityProcessingTalkTalkChannelProcessor(TalkTalkClient client, TalkTalkContentEntityProcessor<UpdateProgress> processor) {
        this.client = checkNotNull(client);
        this.processor = checkNotNull(processor);
    }
    
    @Override
    public UpdateProgress process(ChannelType channel) throws TalkTalkException {
        return client.processVodList(ItemTypeType.CHANNEL, channel.getId(), new TalkTalkVodEntityProcessor<UpdateProgress>() {
            
            private UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public UpdateProgress getResult() {
                return progress;
            }

            @Override
            public void process(VODEntityType entity) {
                UpdateProgress result;
                logProcessing(entity);
                switch (entity.getItemType()) {
                case BRAND:
                    result = processor.processBrandEntity(entity);
                    break;
                case SERIES:
                    result = processor.processSeriesEntity(entity);
                    break;
                case EPISODE:
                    result = processor.processEpisodeEntity(entity);
                    break;
                default:
                    log.warn("Not processing unexpected entity type {}", entity.getItemType());
                    result = UpdateProgress.START;
                    break;
                }
                progress = progress.reduce(result);
            }
        }, _500_ITEMS_PER_PAGE);
    }
    
    private void logProcessing(VODEntityType entity) {
        log.debug("processing {} {}", entity.getItemType(), entity.getId());
    }
    
}
