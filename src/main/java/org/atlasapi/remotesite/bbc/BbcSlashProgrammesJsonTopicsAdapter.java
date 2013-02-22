package org.atlasapi.remotesite.bbc;

import static com.google.common.base.Preconditions.checkArgument;
import static org.atlasapi.media.entity.Publisher.DBPEDIA;

import java.util.List;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.Topic.Type;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesCategory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.metabroadcast.common.base.Maybe;

public class BbcSlashProgrammesJsonTopicsAdapter implements SiteSpecificAdapter<List<TopicRef>> {

    private final RemoteSiteClient<SlashProgrammesContainer> slashProgrammesClient;
    private final TopicStore topicStore;

    public BbcSlashProgrammesJsonTopicsAdapter(RemoteSiteClient<SlashProgrammesContainer> slashProgrammesClient, TopicStore topicStore) {
        this.slashProgrammesClient = slashProgrammesClient;
        this.topicStore = topicStore;
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
        String uri = category.getSameAs();
        String alias = namespace+":"+uri;
        Optional<Topic> existingTopic = topicStore.resolveAliases(ImmutableList.of(alias), Publisher.DBPEDIA).get(alias);
        Topic topic = existingTopic.or(new Topic());
        topic.addAlias(alias);
        topic.setPublisher(DBPEDIA);
        topic.setTitle(category.getTitle());
        topic.setType(topicType);
        topicStore.writeTopic(topic);
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
