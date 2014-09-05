package org.atlasapi.remotesite.events;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.topic.TopicStore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;


public class EventTopicResolverTest {

    private Topic testTopic = Mockito.mock(Topic.class);
    private TopicStore topicStore = Mockito.mock(TopicStore.class);
    private final EventTopicResolver topicResolver = new EventTopicResolver(topicStore);
    
    @Test
    public void testResolvesTopicFromLocationUri() {
        String locationUri = "location uri";
        Mockito.when(topicStore.topicFor("dbpedia", locationUri)).thenReturn(Maybe.just(testTopic));
        
        Topic resolved = topicResolver.createOrResolveVenue("title", locationUri);
        
        Mockito.verify(topicStore).topicFor("dbpedia", locationUri);
        assertEquals(testTopic, resolved);
    }


    @Test
    public void testResolvesTopicsFromEventGroupUris() {
        String sportUri = "sport uri";
        Mockito.when(topicStore.topicFor("dbpedia", sportUri)).thenReturn(Maybe.just(testTopic));
        
        Map<String, String> eventGroupTitleMapping = ImmutableMap.of("a sport", sportUri);
        Set<Topic> resolved = topicResolver.createOrResolveEventGroups(eventGroupTitleMapping);
        
        Mockito.verify(topicStore).topicFor("dbpedia", sportUri);
        assertEquals(testTopic, Iterables.getOnlyElement(resolved));
    }
}
