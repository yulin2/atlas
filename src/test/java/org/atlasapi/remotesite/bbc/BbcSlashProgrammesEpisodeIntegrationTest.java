//package org.atlasapi.remotesite.bbc;
//
//import static org.atlasapi.media.entity.Publisher.DBPEDIA;
//import static org.hamcrest.Matchers.hasItems;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.isA;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.argThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.atlasapi.media.content.Content;
//import org.atlasapi.media.entity.Clip;
//import org.atlasapi.media.entity.KeyPhrase;
//import org.atlasapi.media.entity.Publisher;
//import org.atlasapi.media.entity.RelatedLink;
//import org.atlasapi.media.entity.TopicRef;
//import org.atlasapi.media.topic.Topic;
//import org.atlasapi.media.topic.Topic.Type;
//import org.atlasapi.media.topic.TopicStore;
//import org.atlasapi.media.util.WriteResult;
//import org.atlasapi.persistence.logging.SystemOutAdapterLog;
//import org.atlasapi.remotesite.FixedResponseHttpClient;
//import org.atlasapi.remotesite.SiteSpecificAdapter;
//import org.atlasapi.remotesite.bbc.ion.BbcExtendedDataContentAdapter;
//import org.atlasapi.remotesite.channel4.RecordingContentWriter;
//import org.hamcrest.Description;
//import org.hamcrest.Matcher;
//import org.hamcrest.TypeSafeMatcher;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.mockito.stubbing.Answer;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableSet;
//import com.google.common.io.Resources;
//import com.metabroadcast.common.collect.ImmutableOptionalMap;
//
//@RunWith(MockitoJUnitRunner.class)
//public class BbcSlashProgrammesEpisodeIntegrationTest {
//    
//    private final TopicStore topicStore = mock(TopicStore.class);
//    
//    FixedResponseHttpClient httpClient = FixedResponseHttpClient.respondTo("http://www.bbc.co.uk/programmes/b015d4pt.json", Resources.getResource("bbc-topics-b015d4pt.json"));
//    
//    private final BbcSlashProgrammesJsonTopicsAdapter topicsAdapter = new BbcSlashProgrammesJsonTopicsAdapter(new BbcModule().jsonClient(httpClient), topicStore);
//    private final BbcExtendedDataContentAdapter extendedDataAdapter = new BbcExtendedDataContentAdapter(nullAdapter((List<RelatedLink>)ImmutableList.<RelatedLink>of()), nullAdapter((List<KeyPhrase>)ImmutableList.<KeyPhrase>of()), topicsAdapter);
//
//    @Test
//    public void testClientGetsEpisode() throws Exception {
//        
//        RecordingContentWriter writer = new RecordingContentWriter();
//        
//        BbcProgrammeAdapter adapter = new BbcProgrammeAdapter(writer, extendedDataAdapter, new SystemOutAdapterLog());
//        
//        when(topicStore.resolveAliases(argThat(hasItems(any(String.class))),argThat(isA(Publisher.class))))
//            .thenReturn(ImmutableOptionalMap.<String, Topic>of());
//        final AtomicInteger id = new AtomicInteger(1);
//        when((topicStore).writeTopic(argThat(is(any(Topic.class)))))
//            .thenAnswer(new Answer<WriteResult<Topic>>() {
//                @Override
//                public WriteResult<Topic> answer(InvocationOnMock invocation)
//                    throws Throwable {
//                    Topic topic = (Topic) invocation.getArguments()[0];
//                    topic.setId(id.getAndIncrement());
//                    return WriteResult.written(topic).build();
//                }
//            });
//        
//        Content programme = (Content) adapter.fetch("http://www.bbc.co.uk/programmes/b015d4pt");
//        assertNotNull(programme);
//        
//        assertNotNull(programme.getClips());
//        assertFalse(programme.getClips().isEmpty());
//        assertTrue(programme.getImage().contains("b015d4pt"));
//        assertTrue(programme.getThumbnail().contains("b015d4pt"));
//        
//        for (Clip clip: programme.getClips()) {
//            assertNotNull(clip.getCanonicalUri());
//            assertNotNull(clip.getVersions());
//            assertFalse(clip.getVersions().isEmpty());
//        }
//        
//        //topics are disabled currently
//        TopicRef topic1 = new TopicRef(3l, 1.0f, true, TopicRef.Relationship.ABOUT);
//        TopicRef topic2 = new TopicRef(1l, 1.0f, true, TopicRef.Relationship.ABOUT);
//        TopicRef topic3 = new TopicRef(2l, 1.0f, true, TopicRef.Relationship.ABOUT);
//        TopicRef topic4 = new TopicRef(4l, 1.0f, true, TopicRef.Relationship.ABOUT);
//        
//        assertEquals(ImmutableSet.of(topic1, topic2, topic3, topic4), ImmutableSet.copyOf(programme.getTopicRefs()));
//        
//        String ns = DBPEDIA.name().toLowerCase();
//        String religion = "http://dbpedia.org/resource/Religion";
//        String roshHashanah = "http://dbpedia.org/resource/Rosh_Hashanah";
//        String jonathanSacks = "http://dbpedia.org/resource/Jonathan_Sacks";
//        String debate = "http://dbpedia.org/resource/Debate";
//        
//        verify(topicStore).writeTopic(argThat(is((topicMatcher(1, ns, religion, "Religion", Type.SUBJECT)))));
//        verify(topicStore).writeTopic(argThat(is((topicMatcher(1, ns, roshHashanah, "Rosh Hashanah", Type.SUBJECT)))));
//        verify(topicStore).writeTopic(argThat(is((topicMatcher(1, ns, jonathanSacks, "Jonathan Sacks", Type.PERSON)))));
//        verify(topicStore).writeTopic(argThat(is((topicMatcher(1, ns, debate, "Debate", Type.SUBJECT)))));
//    }
//    
//    private <T> SiteSpecificAdapter<T> nullAdapter(final T returns) {
//        return new SiteSpecificAdapter<T>() {
//
//            @Override
//            public T fetch(String uri) {
//                return returns;
//            }
//
//            @Override
//            public boolean canFetch(String uri) {
//                return true;
//            }
//        };
//    }
//
//    private Matcher<Topic> topicMatcher(final long id, final String ns, final String value, final String title, final Type type) {
//        return new TypeSafeMatcher<Topic>() {
//
//            @Override
//            public void describeTo(Description desc) {
//                desc.appendText(String.format("Matcher %s (%s), %s:%s - %s", id, type, ns, value, title));
//            }
//
//            @Override
//            public boolean matchesSafely(Topic topic) {
//                return topic.getId().equals(id)
//                    && topic.getAliases().contains(ns+":"+value)
//                    && topic.getTitle().equals(title)
//                    && topic.getPublisher().equals(Publisher.DBPEDIA)
//                    && topic.getType().equals(type);
//            }
//        };
//    }
//}
