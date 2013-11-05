package org.atlasapi.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Map.Entry;

import org.atlasapi.application.users.Role;
import org.atlasapi.application.users.User;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.attribute.Attribute;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.atlasapi.output.useraware.UserAwareQueryResult;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.atlasapi.query.annotation.ActiveAnnotations;
import org.atlasapi.query.common.AttributeCoercers;
import org.atlasapi.query.common.QueryAtomParser;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.useraware.UserAwareQuery;
import org.atlasapi.query.common.useraware.UserAwareQueryContext;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


public class SourceRequestQueryExecutorTest {
    
    private SourceRequestQueryExecutor executor;
    private SourceRequest sourceRequest1;
    private SourceRequest sourceRequest2;
    
    @Before
    public void setUp() {
        SourceRequestStore store = mock(SourceRequestStore.class);
        sourceRequest1 = SourceRequest.builder()
                .withAppId(Id.valueOf(5000))
                .withEmail("me@example.com")
                .withApproved(false)
                .withReason("reason 1")
                .withUsageType(UsageType.COMMERCIAL)
                .build();
        sourceRequest2 = SourceRequest.builder()
                .withAppId(Id.valueOf(6000))
                .withEmail("other@example.com")
                .withApproved(false)
                .withReason("reason 2")
                .withUsageType(UsageType.COMMERCIAL)
                .build();
        when(store.sourceRequestFor(Id.valueOf(5000))).thenReturn(Optional.of(sourceRequest1));
        when(store.sourceRequestFor(Id.valueOf(6000))).thenReturn(Optional.of(sourceRequest2));
        when(store.all()).thenReturn(ImmutableSet.of(sourceRequest1, sourceRequest2));
        executor = new SourceRequestQueryExecutor(store);
    }
    
    @Test
    public void testExecutingAllSourceRequestQuery() throws QueryExecutionException {
        User user = User.builder().withId(Id.valueOf(5000)).withRole(Role.ADMIN).build();
        UserAwareQueryContext context = new UserAwareQueryContext(ApplicationSources.defaults(), 
                ActiveAnnotations.standard(),
                Optional.of(user));
        AttributeQuerySet emptyAttributeQuerySet = new AttributeQuerySet(ImmutableSet.<AttributeQuery<?>>of());
        UserAwareQuery<SourceRequest> query = UserAwareQuery.listQuery(emptyAttributeQuerySet, context);
        UserAwareQueryResult<SourceRequest> result = executor.execute(query);
        assertTrue(result.isListResult());
        assertTrue(result.getResources().contains(sourceRequest1));
    }
}
