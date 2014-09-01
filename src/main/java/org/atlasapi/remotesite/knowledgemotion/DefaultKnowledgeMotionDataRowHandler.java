package org.atlasapi.remotesite.knowledgemotion;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.ContentMerger;
import org.atlasapi.remotesite.ContentMerger.MergeStrategy;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class DefaultKnowledgeMotionDataRowHandler implements KnowledgeMotionDataRowHandler {

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final ContentExtractor<KnowledgeMotionDataRow, Optional<? extends Content>> extractor;
    private final ContentMerger contentMerger;

    public DefaultKnowledgeMotionDataRowHandler(ContentResolver resolver, ContentWriter writer, 
            ContentExtractor<KnowledgeMotionDataRow, Optional<? extends Content>> extractor, final TopicQueryResolver topicStore) {
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
        this.extractor = checkNotNull(extractor);
        this.contentMerger = new ContentMerger(MergeStrategy.MERGE){
            @Override
            public Item merge(Item current, Item extracted) {
                Item merged = super.merge(current, extracted);

                HashMap<Long, TopicRef> mergedRefs = new HashMap<Long, TopicRef>();
                for (TopicRef topicRef : merged.getTopicRefs()) {
                    if (topicStore.topicForId(topicRef.getTopic()).hasValue()){
                        // this check is only needed because at one point i accidentally created refs to topics that weren't written properly
                        mergedRefs.put(topicRef.getTopic(), topicRef);
                    }
                }
                for (TopicRef topicRef : extracted.getTopicRefs()) {
                    mergedRefs.put(topicRef.getTopic(), topicRef);
                }

                merged.setTopicRefs(mergedRefs.values());
                return merged;
            }
            // TODO support merging TopicRefs in ContentMerger -- but remember to handle offsets!
        };
    }

    @Override
    public void handle(KnowledgeMotionDataRow row) {
        Content content = extractor.extract(row).orNull();
        if (content == null) {
            return;
        }

        Maybe<Identified> existing = resolve(content.getCanonicalUri());
        if (existing.isNothing()) {
            write(content);
        } else {
            Identified identified = existing.requireValue();
            write(contentMerger.merge(ContentMerger.asItem(identified), (Item) content));
        }
    }

    private void write(Content content) {
        Item item = (Item) content;
        writer.createOrUpdate(item);
    }

    private Maybe<Identified> resolve(String uri) {
        ImmutableSet<String> uris = ImmutableSet.of(uri);
        return resolver.findByCanonicalUris(uris).get(uri);
    }

}
