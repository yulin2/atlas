package org.atlasapi.remotesite.bbc;

import static org.atlasapi.media.entity.Publisher.DBPEDIA;
import static org.atlasapi.media.topic.Topic.Type.SUBJECT;

import java.util.Set;

import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.Topic.Type;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.media.util.WriteResult;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class BbcSlashProgrammesRdfTopicExtractor implements ContentExtractor<SlashProgrammesRdf, Maybe<TopicRef>> {

    private final TopicStore topicStore;

    public BbcSlashProgrammesRdfTopicExtractor(TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    @Override
    public Maybe<TopicRef> extract(SlashProgrammesRdf source) {
        String namespace = Publisher.DBPEDIA.name().toLowerCase();
        String uri = extractTopicUri(source);
        Alias alias = new Alias(namespace, uri);
        Optional<Topic> existingTopic = topicStore.resolveAliases(ImmutableList.of(alias), Publisher.DBPEDIA).get(alias);
        Topic topic = existingTopic.or(new Topic());
        topic.addAlias(alias);
        topic.setPublisher(DBPEDIA);
        topic.setTitle(titleFrom(uri));
        topic.setType(Type.fromKey(typeKeyFrom(source.description()), SUBJECT));
        WriteResult<Topic> writtenTopic = topicStore.writeTopic(topic);
        return Maybe.just(new TopicRef(writtenTopic.getResource().getId(), 1.0f, true, TopicRef.Relationship.ABOUT));
    }

    private String typeKeyFrom(SlashProgrammesDescription description) {
        Set<SlashProgrammesType> type = description.getType();
        if (!(type == null || type.isEmpty())) {
            String uri = Iterables.get(type, 0).resourceUri();
            return uri.substring(uri.lastIndexOf("/") + 1).toLowerCase();
        }
        return Type.SUBJECT.key();
    }

    private String titleFrom(String dbpediaUri) {
        return dbpediaUri.substring(28)
            .replaceAll("_", " ")
            .replace("%28", "(")
            .replace("%29", ")")
            .replace("%27", "'");
    }

    private String extractTopicUri(SlashProgrammesRdf source) {
        SlashProgrammesDescription desc = source.description();
        return desc.getSameAs() != null && !desc.getSameAs().isEmpty() ? Iterables.get(desc.getSameAs(), 0).resourceUri() : null;
    }
}
