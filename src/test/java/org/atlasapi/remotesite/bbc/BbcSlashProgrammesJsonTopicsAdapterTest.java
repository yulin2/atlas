package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicRef;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.media.topic.Topic.Type;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesCategory;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesProgramme;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

@RunWith(JMock.class)
public class BbcSlashProgrammesJsonTopicsAdapterTest extends TestCase {

    private final Mockery context = new Mockery();
    private static String URI = "http://www.bbc.co.uk/programmes/b0144pvg.json";
    
    private final TopicStore topicStore = context.mock(TopicStore.class);
    private @SuppressWarnings("unchecked") final RemoteSiteClient<SlashProgrammesContainer> containerFetcher = context.mock(RemoteSiteClient.class);

    
    BbcSlashProgrammesJsonTopicsAdapter adapter = new BbcSlashProgrammesJsonTopicsAdapter(containerFetcher, topicStore, new NullAdapterLog());
    
    @Test
    public void testCreatesRefAndStoresValidTopic() throws Exception {
        
        final String value = "http://dbpedia.org/resource/Brighton";
        final String namespace = "dbpedia";
        final String title = "Brighton";
        final long id = 1234l;
        
        context.checking(new Expectations() {{
                one(containerFetcher).get(URI);will(returnValue(containerWithTopic("place", title, value)));
                one(topicStore).topicFor(namespace, value); will(returnValue(Maybe.just(topicWithId(id))));
                one(topicStore).write(with(topicMatching(id, Topic.Type.PLACE, title, namespace, value)));
        }});
        
        List<TopicRef> refs = adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        
        assertThat(refs.get(0).getTopic(),is(id));
        assertThat(refs.get(0).getWeighting(), is(1.0f));
        assertThat(refs.get(0).isSupervised(), is(true));
        
    }
    
    @Test
    public void testIgnoresCategoryWithInvalidType() throws Exception {
        
        final String invalidType = "invalidtype";
        
        context.checking(new Expectations() {{
                one(containerFetcher).get(URI);will(returnValue(containerWithTopic(invalidType, "title", "sameAs")));
                never(topicStore).topicFor(with(any(String.class)), with(any(String.class)));
                never(topicStore).write(with(any(Topic.class)));
        }});
        
        List<TopicRef> refs = adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        assertTrue(refs.isEmpty());
        
    }
    
    @Test
    public void testIgnoresCategoryWithNonDbpediaSameAs() throws Exception {
        
        final String value = "http://anothertopicsource.org/resource/Brighton";
        
        context.checking(new Expectations() {{
                one(containerFetcher).get(URI);will(returnValue(containerWithTopic("place", "title", value)));
                never(topicStore).topicFor(with(any(String.class)), with(any(String.class)));
                never(topicStore).write(with(any(Topic.class)));
        }});
        
        List<TopicRef> refs = adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        assertTrue(refs.isEmpty());
    }
    
    private TypeSafeMatcher<Topic> topicMatching(final long id, final Type place, final String title, final String namespace, final String value) {
        return new TypeSafeMatcher<Topic>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText(String.format("topic with id %s, place %s, title %s, namespace %s, value %s",id,place,title,namespace,value));
            }

            @Override
            public boolean matchesSafely(Topic topic) {
                return topic.getId().equals(id)
                    && topic.getType().equals(place)
                    && topic.getTitle().equals(title)
                    && topic.getNamespace().equals(namespace)
                    && topic.getValue().equals(value);
            }
        };
    }
    
    private Topic topicWithId(long id) {
        return new Topic(id);
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
