package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * TalkTalkChannelProcessor which fetches the VOD entity list for the channel
 * and processes each entity using the given TalkTalkContentEntityProcessor.
 */
public class VodEntityProcessingTalkTalkVodListProcessor implements
        TalkTalkChannelProcessor<List<Content>> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int _500_ITEMS_PER_PAGE = 500;

    private final TalkTalkClient client;
    private final TalkTalkContentEntityProcessor<List<Content>> processor;

    public VodEntityProcessingTalkTalkVodListProcessor(TalkTalkClient client, TalkTalkContentEntityProcessor<List<Content>> processor) {
        this.client = checkNotNull(client);
        this.processor = checkNotNull(processor);
    }

    @Override
    public List<Content> process(ChannelType channel) throws TalkTalkException {
        return client.processVodList(ItemTypeType.CHANNEL, channel.getId(), new TalkTalkVodEntityProcessor<List<Content>>() {

            private Builder<Content> contentListBuilder = ImmutableList.builder();

            @Override
            public List<Content> getResult() {
                return contentListBuilder.build();
            }

            @Override
            public void process(VODEntityType entity) {
                logProcessing(entity);
                switch (entity.getItemType()) {
                case BRAND:
                    contentListBuilder.addAll(processor.processBrandEntity(entity));
                    break;
                case SERIES:
                    contentListBuilder.addAll(processor.processSeriesEntity(entity));
                    break;
                case EPISODE:
                    contentListBuilder.addAll(processor.processEpisodeEntity(entity));
                    break;
                case IMAGE:
                    contentListBuilder.addAll(processor.processEpisodeEntity(entity));
                    break;
                default:
                    log.warn("Not processing unexpected entity type {}", entity.getItemType());
                    break;
                }
            }
        }, _500_ITEMS_PER_PAGE);
    }

    private void logProcessing(VODEntityType entity) {
        log.debug("processing {} {}", entity.getItemType(), entity.getId());
    }
}
