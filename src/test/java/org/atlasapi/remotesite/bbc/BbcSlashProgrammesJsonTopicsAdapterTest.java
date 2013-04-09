package org.atlasapi.remotesite.bbc;

import static org.atlasapi.media.entity.Publisher.DBPEDIA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.Topic.Type;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.media.util.WriteResult;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesCategory;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesProgramme;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.collect.ImmutableOptionalMap;

@RunWith(MockitoJUnitRunner.class)
public class BbcSlashProgrammesJsonTopicsAdapterTest extends TestCase {

    private static String URI = "http://www.bbc.co.uk/programmes/b0144pvg.json";
    
    private final TopicStore topicStore = mock(TopicStore.class);
    @SuppressWarnings("unchecked") 
    private final RemoteSiteClient<SlashProgrammesContainer> containerFetcher = mock(RemoteSiteClient.class);

    
    BbcSlashProgrammesJsonTopicsAdapter adapter = new BbcSlashProgrammesJsonTopicsAdapter(containerFetcher, topicStore);
    
    @Test
    public void testWithValidTopic() throws Exception {
        
        final String value = "http://dbpedia.org/resource/Brighton";
        final String namespace = Publisher.DBPEDIA.name().toLowerCase();
        final String title = "Brighton";
        final Id id = Id.valueOf(1234);
        
        when((containerFetcher).get(URI))
            .thenReturn(containerWithTopic("place", title, value));
        when((topicStore).resolveAliases(ImmutableList.of(new Alias(namespace, value)), DBPEDIA))
            .thenReturn(ImmutableOptionalMap.<Alias, Topic>of());
        when((topicStore).writeTopic(argThat(isA(Topic.class))))
            .thenAnswer(new Answer<WriteResult<Topic>>() {
                @Override
                public WriteResult<Topic> answer(InvocationOnMock invocation) throws Throwable {
                    Topic topic = (Topic) invocation.getArguments()[0];
                    topic.setId(id);
                    return WriteResult.written(topic).build();
                }
            });
        
        List<TopicRef> refs = adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        
        verify(topicStore).writeTopic(argThat(is(topicMatching(id, Topic.Type.PLACE, title, namespace, value))));
        
        assertThat(refs.get(0).getTopic(),is(id));
        assertThat(refs.get(0).getWeighting(), is(1.0f));
        assertThat(refs.get(0).isSupervised(), is(true));
        assertThat(refs.get(0).getRelationship(), is(TopicRef.Relationship.ABOUT));
    }
    
    @Test
    public void testIgnoresCategoryWithInvalidType() throws Exception {
        
        final String invalidType = "invalidtype";
        
        when((containerFetcher).get(URI))
            .thenReturn(containerWithTopic(invalidType, "title", "sameAs"));
        verify(topicStore, never()).resolveAliases(
            argThat(Matchers.<Alias>hasItems(instanceOf(Alias.class))), 
            argThat(isA(Publisher.class))
        );
        verify(topicStore, never()).writeTopic(argThat(isA(Topic.class)));

        List<TopicRef> refs = adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        assertTrue(refs.isEmpty());
        
    }
    
    @Test
    public void testIgnoresCategoryWithNonDbpediaSameAs() throws Exception {
        
        final String value = "http://anothertopicsource.org/resource/Brighton";
        
        when((containerFetcher).get(URI))
            .thenReturn(containerWithTopic("place", "title", value));
        verify(topicStore, never()).resolveAliases(
            argThat(Matchers.<Alias>hasItems(instanceOf(Alias.class))), 
            argThat(isA(Publisher.class))
        );
        verify(topicStore, never()).writeTopic(argThat(is(isA(Topic.class))));

        
        List<TopicRef> refs = adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        assertTrue(refs.isEmpty());
    }
    
    private TypeSafeMatcher<Topic> topicMatching(final Id id, final Type place, final String title, final String namespace, final String value) {
        return new TypeSafeMatcher<Topic>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText(String.format("topic with id %s, place %s, title %s, namespace %s, value %s",id,place,title,namespace,value));
            }

            @Override
            public boolean matchesSafely(Topic topic) {
                return topic.getId().equals(id)
                    && topic.getAliases().contains(new Alias(namespace, value))
                    && topic.getType().equals(place)
                    && topic.getTitle().equals(title);
            }
        };
    }
    
    private SlashProgrammesContainer containerWithTopic(String type, String title, String value) {
        SlashProgrammesContainer container = new SlashProgrammesContainer();
        SlashProgrammesProgramme programme = new SlashProgrammesProgramme();
        SlashProgrammesCategory category = new SlashProgrammesCategory();
        category.setType(type);
        category.setTitle(title);
        category.setSameAs(value);
        programme.setCategories(ImmutableList.of(category));
        container.setProgramme(programme);
        return container;
    }
}
