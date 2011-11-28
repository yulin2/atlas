package org.atlasapi.remotesite.bbc;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Topic.Type.SUBJECT;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.Set;

import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.Topic.Type;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesType;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class BbcSlashProgrammesRdfTopicExtractor implements ContentExtractor<SlashProgrammesRdf, Maybe<Topic>> {

    private static final String DBPEDIA_NAMESPACE = "dbpedia";
    private final TopicStore topicStore;
    private final AdapterLog log;

    public BbcSlashProgrammesRdfTopicExtractor(TopicStore topicStore, AdapterLog log) {
        this.topicStore = topicStore;
        this.log = log;
    }
    
    @Override
    public Maybe<Topic> extract(SlashProgrammesRdf source) {
        
        String topicUri = extractTopicUri(source);
        
        if (topicUri != null) {
            Maybe<Topic> possibleTopic = topicStore.topicFor(DBPEDIA_NAMESPACE, topicUri);
            if(possibleTopic.isNothing()) {
                log.record(warnEntry().withSource(getClass()).withDescription("Couldn't get Topic for %s, can't create new one", topicUri));
            } else {
                Topic topic = possibleTopic.requireValue();
                topic.setValue(topicUri);
                topic.setNamespace(DBPEDIA_NAMESPACE);
                topic.addPublisher(BBC);
                topic.setTitle(titleFrom(topicUri));
                topic.setType(Type.fromKey(typeKeyFrom(source.description()), SUBJECT));
                topicStore.write(topic);
                return Maybe.just(topic);
            }
        }
        return Maybe.nothing();
    }
    
    private String typeKeyFrom(SlashProgrammesDescription description) {
        Set<SlashProgrammesType> type = description.getType();
        if(!(type == null || type.isEmpty())) {
            String uri = Iterables.get(type, 0).resourceUri();
            return uri.substring(uri.lastIndexOf("/")+1).toLowerCase();
        }
        return Type.SUBJECT.key();
    }

    private String titleFrom(String dbpediaUri) {
        return dbpediaUri.substring(28).replaceAll("_", " ").replace("%28","(").replace("%29",")").replace("%27","'");
    }

    private String extractTopicUri(SlashProgrammesRdf source) {
        SlashProgrammesDescription desc = source.description();
        return desc.getSameAs() != null && !desc.getSameAs().isEmpty() ? Iterables.get(desc.getSameAs(), 0).resourceUri() : null;
    }

}
