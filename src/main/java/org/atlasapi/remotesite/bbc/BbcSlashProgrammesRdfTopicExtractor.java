package org.atlasapi.remotesite.bbc;

import static org.atlasapi.media.entity.Publisher.DBPEDIA;
import static org.atlasapi.media.topic.Topic.Type.SUBJECT;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.Set;

import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesType;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.Topic.Type;

public class BbcSlashProgrammesRdfTopicExtractor implements ContentExtractor<SlashProgrammesRdf, Maybe<TopicRef>> {

    private final TopicStore topicStore;
    private final AdapterLog log;

    public BbcSlashProgrammesRdfTopicExtractor(TopicStore topicStore, AdapterLog log) {
        this.topicStore = topicStore;
        this.log = log;
    }

    @Override
    public Maybe<TopicRef> extract(SlashProgrammesRdf source) {
        String namespace = Publisher.DBPEDIA.name().toLowerCase();
        String uri = extractTopicUri(source);
        Topic topic = topicStore.topicFor(namespace, uri).valueOrNull();
        if (topic == null) {
            throw new IllegalStateException("This should never happen, as topic is either found or created by the topic store, so failing fast.");
        } else {
            topic.setValue(uri);
            topic.setNamespace(namespace);
            topic.setPublisher(DBPEDIA);
            topic.setTitle(titleFrom(uri));
            topic.setType(Type.fromKey(typeKeyFrom(source.description()), SUBJECT));
            topicStore.write(topic);
        }
        return Maybe.just(new TopicRef(topic.getId(), 1.0f, true, TopicRef.Relationship.ABOUT));
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
        return dbpediaUri.substring(28).replaceAll("_", " ").replace("%28", "(").replace("%29", ")").replace("%27", "'");
    }

    private String extractTopicUri(SlashProgrammesRdf source) {
        SlashProgrammesDescription desc = source.description();
        return desc.getSameAs() != null && !desc.getSameAs().isEmpty() ? Iterables.get(desc.getSameAs(), 0).resourceUri() : null;
    }
}
