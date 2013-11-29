package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.talktalk.TalkTalkClient.TalkTalkVodListCallback;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * TalkTalkChannelProcessor which fetches the VOD entity list for the channel
 * and processes each entity using the given TalkTalkContentEntityProcessor.
 */
public class VodEntityProcessingTalkTalkChannelProcessor implements
        TalkTalkChannelProcessor<UpdateProgress> {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final int NO_FAILURES = 0;
    
    private final TalkTalkClient client;
    private final TalkTalkVodEntityProcessor<List<Content>> processor;

    public VodEntityProcessingTalkTalkChannelProcessor(TalkTalkClient client, TalkTalkVodEntityProcessor<List<Content>> processor) {
        this.client = checkNotNull(client);
        this.processor = checkNotNull(processor);
    }
    
    @Override
    public UpdateProgress process(ChannelType channel, final Optional<ContentGroup> contentGroup) throws TalkTalkException {
        return client.processVodList(GroupType.CHANNEL, channel.getId(), 
            new TalkTalkVodListCallback<UpdateProgress>() {
            
                private UpdateProgress totalProgress = UpdateProgress.START;
                
                @Override
                public UpdateProgress getResult() {
                    return totalProgress;
                }
    
                @Override
                public void process(VODEntityType entity) {
                    log.debug("processing {} {}", entity.getItemType(), entity.getId());
                    List<Content> extracted = processor.processEntity(entity);
                    
                    if (contentGroup.isPresent()) {
                        contentGroup.get().addContents(
                                Iterables.transform(Iterables.filter(extracted, Item.class), TO_CHILD_REF)
                        );
                    }
                    
                    UpdateProgress progress = extracted.isEmpty() ? UpdateProgress.FAILURE
                                                                  : new UpdateProgress(extracted.size(), NO_FAILURES);
                    totalProgress = totalProgress.reduce(progress);
                }
        });
    }
    
    private static Function<Item, ChildRef> TO_CHILD_REF = new Function<Item, ChildRef>() {

        @Override
        public ChildRef apply(Item item) {
            return item.childRef();
        }
        
    };
}
