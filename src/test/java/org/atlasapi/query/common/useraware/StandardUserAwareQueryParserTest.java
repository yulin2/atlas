package org.atlasapi.query.common.useraware;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletRequest;
import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationSources;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.application.users.Role;
import org.atlasapi.application.users.User;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.annotation.ActiveAnnotations;
import org.atlasapi.query.common.AttributeCoercers;
import org.atlasapi.query.common.InvalidOperatorException;
import org.atlasapi.query.common.InvalidParameterException;
import org.atlasapi.query.common.QueryAtomParser;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.servlet.StubHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class StandardUserAwareQueryParserTest {

    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final SourceIdCodec sourceIdCodec = new SourceIdCodec(idCodec);
    private final QueryAttributeParser parser = new QueryAttributeParser(
        ImmutableList.of(
            QueryAtomParser.valueOf(Attributes.ID, AttributeCoercers.idCoercer(idCodec)), 
            QueryAtomParser.valueOf(Attributes.SOURCE_READS, AttributeCoercers.sourceIdCoercer(sourceIdCodec))
        )
    );
    
    private final UserAwareQueryContextParser queryContextParser = mock(UserAwareQueryContextParser.class);
    private StandardUserAwareQueryParser<Application> queryParser; 
    
    @Before
    public void setUp() {
        when(queryContextParser.getParameterNames()).thenReturn(ImmutableSet.<String>of());
        queryParser = new StandardUserAwareQueryParser<Application>(Resource.APPLICATION, parser, idCodec, queryContextParser);
    }
    
    @Test
    public void testParsesSingleIdIntoNonListApplicationQuery() throws Exception {
        when(queryContextParser.parseSingleContext(isA(HttpServletRequest.class)))
            .thenReturn(UserAwareQueryContext.standard());
        
        UserAwareQuery<Application> q = queryParser.parse(requestWithPath("4.0/applications/ks9.json"));
        
        assertFalse(q.isListQuery());
        assertThat(q.getOnlyId(), is(Id.valueOf(idCodec.decode("ks9"))));
        
        verify(queryContextParser).parseSingleContext(isA(HttpServletRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParsesIdsOnlyIntoListQuery() throws Exception {
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
            .thenReturn(UserAwareQueryContext.standard());
    
        UserAwareQuery<Application> q = queryParser.parse(requestWithPath("4.0/applications.json")
            .withParam("id", "ks9"));
        
        assertTrue(q.isListQuery());
        assertThat(q.getOperands().size(), is(1));
        AttributeQuery<Id> operand = (AttributeQuery<Id>) Iterables.getOnlyElement(q.getOperands());
        assertThat(operand.getValue(), hasItem(Id.valueOf(idCodec.decode("ks9"))));
        
        verify(queryContextParser).parseListContext(isA(HttpServletRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParsesAttributeKeyWithOperator() throws Exception {
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
            .thenReturn(UserAwareQueryContext.standard());
        
        UserAwareQuery<Application> q = queryParser.parse(requestWithPath("4.0/applications.json")
                .withParam("source.reads", "cpc"));
        
        assertTrue(q.isListQuery());
        assertThat(q.getOperands().size(), is(1));
        AttributeQuery<Publisher> operand = (AttributeQuery<Publisher>) Iterables.getOnlyElement(q.getOperands());
        assertThat(operand.getValue(), hasItem(Publisher.BBC));
        
        verify(queryContextParser).parseListContext(isA(HttpServletRequest.class));
    }

    @Test(expected=InvalidParameterException.class)
    public void testRejectsInvalidAttributeKey() throws Exception {
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
        .thenReturn(UserAwareQueryContext.standard());
        
        queryParser.parse(requestWithPath("4.0/applications.json")
                .withParam("some.prefix", "cpc"));
        
    }
    
    @Test(expected=InvalidOperatorException.class)
    public void testRejectsAttributeKeyWithBadOperator() throws Exception {
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
        .thenReturn(UserAwareQueryContext.standard());
        
        queryParser.parse(requestWithPath("4.0/applications.json")
                .withParam("source.reads.invalidbit", "cpc"));
        
    }
    
    @Test
    public void testUserIsAdmin() throws Exception {
        final User user = User.builder().withId(Id.valueOf(5000))
                .withRole(Role.ADMIN)
                .build();
                
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
        .thenReturn(new UserAwareQueryContext(
                ApplicationSources.defaults(), 
                ActiveAnnotations.standard(),
                Optional.of(user))
        );
        UserAwareQuery<Application> q = queryParser.parse(requestWithPath("4.0/applications.json"));
        assertTrue(q.getContext().isAdminUser());
        assertEquals(q.getContext().getUser().get(), user);
    }
    
    @Test
    public void testUserNotAdmin() throws Exception {
        final User user = User.builder().withId(Id.valueOf(6000))
                .withRole(Role.REGULAR)
                .build();
                
        when(queryContextParser.parseListContext(isA(HttpServletRequest.class)))
        .thenReturn(new UserAwareQueryContext(
                ApplicationSources.defaults(), 
                ActiveAnnotations.standard(),
                Optional.of(user))
        );
        UserAwareQuery<Application> q = queryParser.parse(requestWithPath("4.0/applications.json"));
        assertFalse(q.getContext().isAdminUser());
        assertEquals(q.getContext().getUser().get(), user);
    }

    private StubHttpServletRequest requestWithPath(String uri) {
        return new StubHttpServletRequest().withRequestUri(uri);
    }

}
