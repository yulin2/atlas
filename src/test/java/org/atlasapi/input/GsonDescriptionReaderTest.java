package org.atlasapi.input;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.TopicRef.Relationship;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.TopicRef;
import org.atlasapi.media.entity.testing.ItemTestDataBuilder;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.JsonTranslator;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.servlet.StubHttpServletRequest;
import com.metabroadcast.common.servlet.StubHttpServletResponse;

public class GsonDescriptionReaderTest {

    @Test
    public void test() throws Exception {
        Item testItem = ItemTestDataBuilder.item().build();
        TopicRef topicRef = new TopicRef();
        topicRef.setRelationship("about");
        testItem.setTopics(ImmutableSet.of(topicRef));
        
        JsonTranslator<Item> writer = new JsonTranslator<Item>();
        
        HttpServletRequest request = new StubHttpServletRequest();
        StubHttpServletResponse response = new StubHttpServletResponse();
        writer.writeTo(request, response, testItem, ImmutableSet.copyOf(Annotation.values()), ApplicationConfiguration.defaultConfiguration());
        
        String respBody = response.getResponseAsString();
        
        DefaultGsonModelReader descReader = new DefaultGsonModelReader();
        
        Description desc = descReader.read(new StringReader(respBody), Description.class);
        
        assertThat(desc, is(instanceOf(Item.class)));
        assertThat(desc.getUri(), is(testItem.getUri()));
        assertThat(desc.getTitle(), is(testItem.getTitle()));
        assertThat(desc.getTopics().isEmpty(), is(false));
        
    }

}
