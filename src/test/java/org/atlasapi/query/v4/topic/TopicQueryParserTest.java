package org.atlasapi.query.v4.topic;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.attribute.IdAttribute;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.servlet.StubHttpServletRequest;


public class TopicQueryParserTest {

    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final Map<Attribute<?>, AttributeCoercer<String, ?>> atrributes = ImmutableMap.<Attribute<?>, AttributeCoercer<String, ?>> of(
        new IdAttribute("id", Topic.class, false), new AbstractAttributeCoercer<String, Id>() {
            @Override
            protected Id coerce(String input) {
                return Id.valueOf(idCodec.decode(input));
            }
        }
    );
    
    private final ApplicationConfigurationFetcher appFetcher = mock(ApplicationConfigurationFetcher.class);
    private final SelectionBuilder selectionBuilder = Selection.builder();
    private final TopicQueryParser queryParser = new TopicQueryParser(atrributes, idCodec, appFetcher, selectionBuilder);
    
    @Test
    public void testParsesSingleIdIntoNonListTopicQuery() {
        when(appFetcher.configurationFor(isA(HttpServletRequest.class)))
            .thenReturn(Maybe.<ApplicationConfiguration>nothing());
        
        TopicQuery q = queryParser.queryFrom(requestWithPath("4.0/topics/cbbh.json"));
        
        assertFalse(q.isListQuery());
        assertThat(q.getIdsIfOnly().get().size(), is(1));
        assertThat(q.getIdsIfOnly().get(), hasItem(Id.valueOf(idCodec.decode("cbbh"))));
    }

    @Test
    public void testParsesIdsOnlyIntoListQuery() {
        when(appFetcher.configurationFor(isA(HttpServletRequest.class)))
        .thenReturn(Maybe.<ApplicationConfiguration>nothing());
        
        TopicQuery q = queryParser.queryFrom(requestWithPath("4.0/topics.json")
            .withParam("id", "cbbh"));
        
        assertTrue(q.isListQuery());
        assertThat(q.getIdsIfOnly().get().size(), is(1));
        assertThat(q.getIdsIfOnly().get(), hasItem(Id.valueOf(idCodec.decode("cbbh"))));
    }

    private StubHttpServletRequest requestWithPath(String uri) {
        return new StubHttpServletRequest().withRequestUri(uri);
    }

}
