package org.atlasapi.remotesite.bbc;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.Topic.Type;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.channel4.RecordingContentWriter;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class BbcSlashProgrammesEpisodeIntegrationTest extends TestCase {
    
    Mockery context = new Mockery();
    private final TopicStore topicStore = context.mock(TopicStore.class);

    public void testClientGetsEpisode() throws Exception {
        
        RecordingContentWriter writer = new RecordingContentWriter();
        
        BbcProgrammeAdapter adapter = new BbcProgrammeAdapter(writer, topicStore, new SystemOutAdapterLog());
        
        context.checking(new Expectations(){{
            oneOf(topicStore).topicFor("dbpedia", "http://dbpedia.org/resource/Religion"); will(returnValue(newTopic("one", "dbpedia", "http://dbpedia.org/resource/Religion")));
            oneOf(topicStore).write(with(topicMatcher("one","dbpedia", "http://dbpedia.org/resource/Religion", "Religion",Topic.Type.SUBJECT)));
            oneOf(topicStore).topicFor("dbpedia", "http://dbpedia.org/resource/Rosh_Hashanah"); will(returnValue(newTopic("two", "dbpedia", "http://dbpedia.org/resource/Rosh_Hashanah")));
            oneOf(topicStore).write(with(topicMatcher("two","dbpedia", "http://dbpedia.org/resource/Rosh_Hashanah", "Rosh Hashanah",Topic.Type.PERSON)));
            oneOf(topicStore).topicFor("dbpedia", "http://dbpedia.org/resource/Jonathan_Sacks"); will(returnValue(newTopic("three", "dbpedia", "http://dbpedia.org/resource/Jonathan_Sacks")));
            oneOf(topicStore).write(with(topicMatcher("three","dbpedia", "http://dbpedia.org/resource/Jonathan_Sacks", "Jonathan Sacks",Topic.Type.PERSON)));
            
        }});

        Content programme = (Content) adapter.createOrUpdate("http://www.bbc.co.uk/programmes/b015d4pt");
        assertNotNull(programme);
        
        assertNotNull(programme.getClips());
        assertFalse(programme.getClips().isEmpty());
        assertTrue(programme.getImage().contains("b015d4pt"));
        assertTrue(programme.getThumbnail().contains("b015d4pt"));
        
        for (Clip clip: programme.getClips()) {
            assertNotNull(clip.getCanonicalUri());
            assertNotNull(clip.getVersions());
            assertFalse(clip.getVersions().isEmpty());
        }
        
        assertEquals(ImmutableSet.of("one","two","three"), ImmutableSet.copyOf(programme.getTopics()));
    }
    
    private Matcher<Topic> topicMatcher(final String uri, final String ns, final String value, final String title, final Type type) {
        return new TypeSafeMatcher<Topic>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText(String.format("Matcher %s (%s), %s:%s - %s", uri, type, ns, value, title));
            }

            @Override
            public boolean matchesSafely(Topic topic) {
                return topic.getCanonicalUri().equals(uri) &&
                topic.getNamespace().equals(ns) &&
                topic.getValue().equals(value) &&
                topic.getTitle().equals(title) &&
                topic.getType().equals(type);
            }
        };
    }
    
    

    private Maybe<Topic> newTopic(String id, String ns, String value) {
        Topic topic = new Topic(id);
        topic.setNamespace(ns);
        topic.setValue(value);
        return Maybe.just(topic);
    }
}
