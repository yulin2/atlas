package org.atlasapi.query.common;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.servlet.StubHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class StandardQueryParserTest {

    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final QueryAttributeParser atrributes = new QueryAttributeParser(
        ImmutableList.of(QueryAtomParser.valueOf(Attributes.ID, AttributeCoercers.idCoercer(idCodec)))
    );
    
    private final QueryContextParser queryContextParser = mock(QueryContextParser.class);
    private final StandardQueryParser<Topic> queryParser = new StandardQueryParser<Topic>("topics", atrributes, idCodec, queryContextParser);
    
    @Test
    public void testParsesSingleIdIntoNonListTopicQuery() throws Exception {
        when(queryContextParser.parseSingleContext(isA(HttpServletRequest.class)))
            .thenReturn(QueryContext.standard());
        
        Query<Topic> q = queryParser.parse(requestWithPath("4.0/topics/cbbh.json"));
        
        assertFalse(q.isListQuery());
        assertThat(q.getOnlyId(), is(Id.valueOf(idCodec.decode("cbbh"))));
        
        verify(queryContextParser).parseSingleContext(isA(HttpServletRequest.class));
    }

    @Test
    public void testParsesIdsOnlyIntoListQuery() throws Exception {
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
            .thenReturn(QueryContext.standard());
    
        Query<Topic> q = queryParser.parse(requestWithPath("4.0/topics.json")
            .withParam("id", "cbbh"));
        
        assertTrue(q.isListQuery());
        assertThat(q.getOperands().size(), is(1));
        assertThat(Iterables.getOnlyElement(Iterables.getOnlyElement(q.getOperands()).getValue()),
            Matchers.<Object>is(Id.valueOf(idCodec.decode("cbbh"))));
        
        verify(queryContextParser).parseListContext(isA(HttpServletRequest.class));
    }

    private StubHttpServletRequest requestWithPath(String uri) {
        return new StubHttpServletRequest().withRequestUri(uri);
    }

}
