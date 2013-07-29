package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * {@link ScheduledTask} which retrieves the TV Structure via the provided
 * {@link TalkTalkClient} and processes each {@link ChannelType} in turn using
 * the provided {@link TalkTalkChannelProcessor}.
 */
public class TalkTalkVodContentListUpdateTask extends ScheduledTask {

    private final String TALK_TALK_URI_PART = "http://talktalk.net/groups";
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TalkTalkClient client;
    private final ContentGroupWriter writer;
    private final ContentGroupResolver resolver;
    private final TalkTalkVodEntityProcessor<List<Content>> processor;

    private GroupType type;
    private String identifier;


    public TalkTalkVodContentListUpdateTask(TalkTalkClient talkTalkClient,
            ContentGroupResolver resolver, ContentGroupWriter writer,
            TalkTalkVodEntityProcessor<List<Content>> entityProcessor,
            GroupType type, String identifier) {
        this.client = checkNotNull(talkTalkClient);
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
        this.processor = checkNotNull(entityProcessor);
        this.type = type;
        this.identifier = identifier;
    }

    @Override
    protected void runTask() {
        try {
            
            ContentGroup group = resolveOrCreateContentGroup();
            List<ChildRef> refs = client.processVodList(type, identifier, new TalkTalkVodListProcessor<List<ChildRef>>() {

                private UpdateProgress progress = UpdateProgress.START;
                private ImmutableList.Builder<ChildRef> refs = ImmutableList.builder();

                @Override
                public List<ChildRef> getResult() {
                    return refs.build();
                }

                @Override
                public void process(VODEntityType entity) {
                    log.debug("processing entity {}", entity.getId());
                    List<Content> contentList = processor.processEntity(entity);
                    if (!contentList.isEmpty()) {
                        // This is pretty grim as it gives semantics to
                        // the first element of the list extracted,
                        // hence a ContentHierarchy is a better result TODO
                        Content content = contentList.get(0);
                        refs.add(content.childRef());
                        progress = progress.reduce(UpdateProgress.SUCCESS);
                    } else {
                        progress = progress.reduce(UpdateProgress.FAILURE);
                    }
                    reportStatus(progress.toString());
                }
            });
            
            group.setContents(refs);
            writer.createOrUpdate(group);
            
        } catch (TalkTalkException tte) {
            log.error("content update failed", tte);
            // ensure task is marked failed
            throw new RuntimeException(tte);
        }
    }

    private ContentGroup resolveOrCreateContentGroup() {
        String uri = String.format("%s/%s/%s", TALK_TALK_URI_PART, type.toString().toLowerCase(), identifier);
        ResolvedContent resolvedContent = resolver.findByCanonicalUris(ImmutableList.of(uri));
        Maybe<Identified> possibleGroup = resolvedContent.get(uri);
        return possibleGroup.hasValue() ? (ContentGroup) resolvedContent.get(uri).requireValue() 
                                        : new ContentGroup(uri, Publisher.TALK_TALK);
    }
    
}
