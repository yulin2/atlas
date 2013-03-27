package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.Topic.Type;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.media.util.Resolved;
import org.atlasapi.media.util.WriteResult;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.collect.OptionalMap;

public class BbcSlashProgrammesRdfTopicExtractorTest extends TestCase {

    private final String topicUri = "http://dbpedia.org/resource/Religion";
    private final DummyTopicStore topicStore = new DummyTopicStore(topicUri);
    private final BbcSlashProgrammesRdfTopicExtractor extractor = new BbcSlashProgrammesRdfTopicExtractor(topicStore);

    @Test
    public void testExtractsTopicFromValidSlashProgrammesTopic() {
        
        String typeUri = "http://purl.org/ontology/po/Person";
        
        SlashProgrammesRdf rdf = new SlashProgrammesRdf().withDescription(new SlashProgrammesDescription().withSameAs(
                ImmutableSet.of(new SlashProgrammesRdf.SlashProgrammesSameAs().withResourceUri(topicUri))
        ).withTypes(
                ImmutableSet.of(new SlashProgrammesRdf.SlashProgrammesType().withResourceUri(typeUri))
        ));
        
        Maybe<TopicRef> extractedTopicRef = extractor.extract(rdf);
        Topic storedTopic = topicStore.getStoredTopic();
        
        assertTrue(extractedTopicRef.hasValue());
        assertThat(extractedTopicRef.requireValue().getTopic(), is(equalTo(storedTopic.getId())));
        assertThat(extractedTopicRef.requireValue().getWeighting(), is(equalTo(1f)));
        assertThat(extractedTopicRef.requireValue().isSupervised(), is(true));
        assertThat(extractedTopicRef.requireValue().getRelationship(), is(TopicRef.Relationship.ABOUT));
        
        assertThat(storedTopic.getAliasUrls(), hasItem(Publisher.DBPEDIA.name().toLowerCase()+":"+topicUri));
        assertThat(storedTopic.getType(), is(equalTo(Type.PERSON)));
        assertThat(storedTopic.getPublisher(), is(equalTo(Publisher.DBPEDIA)));
        assertThat(storedTopic.getTitle(), is(equalTo("Religion")));
        
    }
    
    private static class DummyTopicStore implements TopicStore {

        private Topic storedTopic;
        private String topicUri;

        public DummyTopicStore(String topicUri) {
            this.topicUri = topicUri;
        }

        @Override
        public WriteResult<Topic> writeTopic(Topic topic) {
            Preconditions.checkState(topicUri != null, "Already stored a topic");
            this.storedTopic = topic;
            topic.setId(1234L);
            return WriteResult.written(topic).build();
        }

        public Topic getStoredTopic() {
            return storedTopic;
        }

        @Override
        public ListenableFuture<Resolved<Topic>> resolveIds(Iterable<Id> ids) {
            return Futures.immediateFuture(Resolved.valueOf(ImmutableList.<Topic>of()));
        }


        @Override
        public OptionalMap<String, Topic> resolveAliases(Iterable<String> aliases, Publisher source) {
            return ImmutableOptionalMap.fromMap(ImmutableMap.<String,Topic>of());
        }
    }

}
