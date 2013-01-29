package org.atlasapi.remotesite.bbc;

import static com.google.common.base.Preconditions.checkArgument;
import static org.atlasapi.media.entity.Publisher.DBPEDIA;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesCategory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.metabroadcast.common.base.Maybe;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.Topic.Type;

public class BbcSlashProgrammesJsonTopicsAdapter implements SiteSpecificAdapter<List<TopicRef>> {

    private final RemoteSiteClient<SlashProgrammesContainer> slashProgrammesClient;
    private final TopicStore topicStore;
    private final AdapterLog log;

    public BbcSlashProgrammesJsonTopicsAdapter(RemoteSiteClient<SlashProgrammesContainer> slashProgrammesClient, TopicStore topicStore, AdapterLog log) {
        this.slashProgrammesClient = slashProgrammesClient;
        this.topicStore = topicStore;
        this.log = log;
    }

    @Override
    public List<TopicRef> fetch(String uri) {
        checkArgument(canFetch(uri), "Invalid fetch uri" + uri);

        SlashProgrammesContainer programmesContainer;
        try {
            programmesContainer = slashProgrammesClient.get(addJsonSuffix(uri));
        } catch (Exception e) {
            return ImmutableList.of();
        }

        Builder<TopicRef> topicRefs = ImmutableList.builder();
        for (SlashProgrammesCategory category : programmesContainer.getProgramme().getCategories()) {
            Type topicType = Topic.Type.fromKey(category.getType());
            if (topicType != null && isDbpediaLink(category.getSameAs())) {
                TopicRef ref = topicRefFrom(resolveTopic(category, topicType));
                if (ref != null) {
                    topicRefs.add(ref);
                }
            }
        }


        return topicRefs.build();
    }

    private String addJsonSuffix(String uri) {
        return uri + ".json";
    }

    private boolean isDbpediaLink(String sameAs) {
        return sameAs != null && sameAs.startsWith("http://dbpedia.org/resource/");
    }

    public Maybe<Topic> resolveTopic(SlashProgrammesCategory category, Type topicType) {
        String namespace = Publisher.DBPEDIA.name().toLowerCase();
        Topic topic = topicStore.topicFor(namespace, category.getSameAs()).valueOrNull();
        if (topic == null) {
            throw new IllegalStateException("This should never happen, as topic is either found or created by the topic store, so failing fast.");
        } else {
            topic.setValue(category.getSameAs());
            topic.setNamespace(namespace);
            topic.setPublisher(DBPEDIA);
            topic.setTitle(category.getTitle());
            topic.setType(topicType);
            topicStore.write(topic);
        }
        return Maybe.just(topic);
    }

    private TopicRef topicRefFrom(Maybe<Topic> possibleTopic) {
        if (possibleTopic.hasValue()) {
            return new TopicRef(possibleTopic.requireValue(), 1.0f, true, TopicRef.Relationship.ABOUT);
        }
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }
}
