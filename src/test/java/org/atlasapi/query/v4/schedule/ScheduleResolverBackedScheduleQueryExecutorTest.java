package org.atlasapi.query.v4.schedule;

import static org.atlasapi.media.entity.Publisher.METABROADCAST;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.MergingEquivalentsResolver;
import org.atlasapi.equiv.ResolvedEquivalents;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.schedule.ScheduleResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.annotation.ActiveAnnotations;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryResult;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleResolverBackedScheduleQueryExecutorTest {

    @SuppressWarnings("unchecked")
    private final MergingEquivalentsResolver<Content> equivalentContentResolver = mock(MergingEquivalentsResolver.class); 
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ScheduleResolver scheduleResolver = mock(ScheduleResolver.class);
    
    private final ScheduleResolverBackedScheduleQueryExecutor executor
            = new ScheduleResolverBackedScheduleQueryExecutor(channelResolver, scheduleResolver, equivalentContentResolver);
    
    @Test
    public void testExecutingScheduleQuery() throws Exception {
        
        Channel channel = Channel.builder().build();
        channel.setId(1);
        channel.setCanonicalUri("one");
        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        ScheduleQuery query = new ScheduleQuery(METABROADCAST, channel.getId(), interval, QueryContext.standard());

        ChannelSchedule channelSchedule = new ChannelSchedule(channel, interval, ImmutableList.<ItemAndBroadcast>of());

        when(channelResolver.fromId(channel.getId()))
            .thenReturn(Maybe.just(channel));
        when(scheduleResolver.resolve(argThat(hasItems(channel)), eq(interval), eq(query.getSource())))
                .thenReturn(Futures.immediateFuture(new Schedule(ImmutableList.of(channelSchedule), interval)));
        
        QueryResult<ChannelSchedule> result = executor.execute(query);
        
        assertThat(result.getOnlyResource(), is(channelSchedule));
    }
    
    @Test
    public void testThrowsExceptionIfChannelIsMissing() {
        
        when(channelResolver.fromId(any(Id.class)))
            .thenReturn(Maybe.<Channel>nothing());

        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        ScheduleQuery query = new ScheduleQuery(METABROADCAST, Id.valueOf(1), interval, QueryContext.standard());
        
        try {
            executor.execute(query);
            fail("expected NotFoundException");
        } catch (QueryExecutionException qee) {
            assertThat(qee, is(instanceOf(NotFoundException.class)));
            verifyZeroInteractions(scheduleResolver);
        }
    }

    @Test
    public void testResolvesEquivalentContentForApiKeyWithPrecedenceEnabled() throws Exception {
        Channel channel = Channel.builder().build();
        channel.setId(1);
        channel.setCanonicalUri("one");
        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        
        ApplicationConfiguration appConfig = ApplicationConfiguration.defaultConfiguration()
                .copyWithPrecedence(Publisher.all().asList());
        QueryContext context = new QueryContext(appConfig, ActiveAnnotations.standard());
        
        Id itemId = Id.valueOf(1);
        ChannelSchedule channelSchedule = new ChannelSchedule(channel, interval, ImmutableList.<ItemAndBroadcast>of(
            new ItemAndBroadcast(
                new Item(itemId, METABROADCAST), 
                new Broadcast(channel.getCanonicalUri(), interval)
            )
        ));

        ScheduleQuery query = new ScheduleQuery(METABROADCAST, channel.getId(), interval, context);

        Item equivalentItem = new Item(itemId, METABROADCAST);
        when(channelResolver.fromId(channel.getId()))
            .thenReturn(Maybe.just(channel));
        when(scheduleResolver.resolve(argThat(hasItems(channel)), eq(interval), eq(query.getSource())))
            .thenReturn(Futures.immediateFuture(new Schedule(ImmutableList.of(channelSchedule), interval)));
        when(equivalentContentResolver.resolveIds(ImmutableList.of(itemId), appConfig, ActiveAnnotations.standard().all()))
            .thenReturn(Futures.immediateFuture(ResolvedEquivalents.<Content>builder().putEquivalents(itemId, ImmutableList.of(equivalentItem)).build()));
        
        QueryResult<ChannelSchedule> result = executor.execute(query);
        
        assertThat(result.getOnlyResource().getEntries().get(0).getItem(), sameInstance(equivalentItem));
        verify(equivalentContentResolver).resolveIds(ImmutableList.of(itemId), appConfig, ActiveAnnotations.standard().all());
        
    }
    
}
