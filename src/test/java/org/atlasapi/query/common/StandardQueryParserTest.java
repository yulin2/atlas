package org.atlasapi.query.common;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.query.common.AttributeCoercers;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryAtomParser;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.QueryParameterAnnotationsExtractor;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.servlet.StubHttpServletRequest;

public class StandardQueryParserTest {

    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final QueryAttributeParser atrributes = new QueryAttributeParser(
        ImmutableList.of(QueryAtomParser.valueOf(Attributes.ID, AttributeCoercers.idCoercer(idCodec)))
    );
    
    private final ApplicationConfigurationFetcher appFetcher = mock(ApplicationConfigurationFetcher.class);
    private final SelectionBuilder selectionBuilder = Selection.builder();
    private final QueryParameterAnnotationsExtractor annotationExtractor = new QueryParameterAnnotationsExtractor("topic");
    private final StandardQueryParser<Topic> queryParser = new StandardQueryParser<Topic>("topics", atrributes, idCodec, appFetcher, selectionBuilder, annotationExtractor);
    
    @Test
    public void testParsesSingleIdIntoNonListTopicQuery() {
        when(appFetcher.configurationFor(isA(HttpServletRequest.class)))
            .thenReturn(Maybe.<ApplicationConfiguration>nothing());
        
        Query<Topic> q = queryParser.parse(requestWithPath("4.0/topics/cbbh.json"));
        
        assertFalse(q.isListQuery());
        assertThat(q.getOnlyId(), is(Id.valueOf(idCodec.decode("cbbh"))));
    }

    @Test
    public void testParsesIdsOnlyIntoListQuery() {
        when(appFetcher.configurationFor(isA(HttpServletRequest.class)))
            .thenReturn(Maybe.<ApplicationConfiguration>nothing());
        
        Query<Topic> q = queryParser.parse(requestWithPath("4.0/topics.json")
            .withParam("id", "cbbh"));
        
        assertTrue(q.isListQuery());
        assertThat(q.getOperands().size(), is(1));
        assertThat(((AttributeQuery<Id>)Iterables.getOnlyElement(q.getOperands())).getValue(),
            hasItem(Id.valueOf(idCodec.decode("cbbh"))));
    }

    private StubHttpServletRequest requestWithPath(String uri) {
        return new StubHttpServletRequest().withRequestUri(uri);
    }

}
