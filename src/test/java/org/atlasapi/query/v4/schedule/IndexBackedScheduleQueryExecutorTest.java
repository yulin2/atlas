package org.atlasapi.query.v4.schedule;

import static org.atlasapi.media.entity.MediaType.VIDEO;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.METABROADCAST;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.persistence.content.schedule.ScheduleIndex;
import org.atlasapi.persistence.content.schedule.ScheduleRef;
import org.atlasapi.persistence.content.schedule.ScheduleRef.ScheduleRefEntry;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.metabroadcast.common.time.DateTimeZones;
import java.util.Set;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.EquivalentContent;
import org.atlasapi.persistence.content.EquivalentContent.Builder;
import org.atlasapi.persistence.content.EquivalentContentResolver;

@RunWith(MockitoJUnitRunner.class)
public class IndexBackedScheduleQueryExecutorTest {

    private final ScheduleIndex index = mock(ScheduleIndex.class);
    private final EquivalentContentResolver contentQueryExecutor = mock(EquivalentContentResolver.class);
    private final IndexBackedScheduleQueryExecutor queryExecutor = new IndexBackedScheduleQueryExecutor(index, contentQueryExecutor);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDoesntResolveContentOnEmptyScheduleRef() throws Exception {
        Channel channel = new Channel(BBC, "One", "one", VIDEO, "one");
        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        ScheduleQuery query = new ScheduleQuery(METABROADCAST, channel, interval);

        when(index.resolveSchedule(METABROADCAST, channel, interval))
            .thenReturn(Futures.immediateFuture(ScheduleRef.forChannel(channel.getCanonicalUri()).build()));
        
        ScheduleChannel channelSchedule = queryExecutor.execute(query);

        verify(contentQueryExecutor, never()).resolveUris(argThat(any(Iterable.class)), argThat(any(List.class)), argThat(any(Set.class)), eq(false));
        
        assertThat(channelSchedule.channel(), is(channel));
        assertThat(channelSchedule.items().isEmpty(), is(true));
    }

    @Test
    public void testHandlesSimpleScheduleQuery() throws Exception {
        Channel channel = new Channel(BBC, "One", "one", VIDEO, "one");
        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        ScheduleQuery query = new ScheduleQuery(METABROADCAST, channel, interval);
        
        Item item = itemWithBroadcast("item", channel.getCanonicalUri(), dateTime(25), dateTime(75), "bid");
        addBroadcast(item, channel.getCanonicalUri(), dateTime(125), dateTime(175), "bid2");
        
        when(index.resolveSchedule(METABROADCAST, channel, interval))
            .thenReturn(Futures.immediateFuture(
                ScheduleRef.forChannel(channel.getCanonicalUri())
                    .addEntry(new ScheduleRefEntry(item.getCanonicalUri(), channel.getCanonicalUri(), dateTime(25), dateTime(75), "bid"))
                    .build()
            ));
        
        when(contentQueryExecutor.resolveUris(argThat(hasItems(item.getCanonicalUri())), argThat(any(List.class)), argThat(any(Set.class)), eq(false)))
            .thenReturn(queryResult(item.getCanonicalUri(), ImmutableList.<Content>of(item)));
        
        ScheduleChannel channelSchedule = queryExecutor.execute(query);

        verify(contentQueryExecutor).resolveUris(argThat(hasItems(item.getCanonicalUri())), argThat(any(List.class)), argThat(any(Set.class)), eq(false));
        
        assertThat(channelSchedule.channel(), is(channel));
        
        Item scheduleItem = Iterables.getOnlyElement(channelSchedule.items());
        assertThat(scheduleItem.getCanonicalUri(), is(item.getCanonicalUri()));
        Version scheduleVersion = Iterables.getOnlyElement(scheduleItem.getVersions());
        Broadcast scheduleBroadcast = Iterables.getOnlyElement(scheduleVersion.getBroadcasts());
        assertThat(scheduleBroadcast.getSourceId(), is("bid"));
    }
    
    @Test
    public void testRepeatedScheduleItemAppearsTwice() throws Exception {
        Channel channel = new Channel(BBC, "One", "one", VIDEO, "one");
        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        ScheduleQuery query = new ScheduleQuery(METABROADCAST, channel, interval);
        
        Item item = itemWithBroadcast("item", channel.getCanonicalUri(), dateTime(25), dateTime(75), "bid");
        addBroadcast(item, channel.getCanonicalUri(), dateTime(125), dateTime(175), "bid2");
        
        when(index.resolveSchedule(METABROADCAST, channel, interval))
            .thenReturn(Futures.immediateFuture(
                ScheduleRef.forChannel(channel.getCanonicalUri())
                    .addEntry(new ScheduleRefEntry(item.getCanonicalUri(), channel.getCanonicalUri(), dateTime(25), dateTime(75), "bid"))
                    .addEntry(new ScheduleRefEntry(item.getCanonicalUri(), channel.getCanonicalUri(), dateTime(125), dateTime(175), "bid2"))
                    .build()
            ));
        
        when(contentQueryExecutor.resolveUris(argThat(hasItems(item.getCanonicalUri())), argThat(any(List.class)), argThat(any(Set.class)), eq(false)))
        .thenReturn(queryResult(item.getCanonicalUri(), ImmutableList.<Content>of(item)));
    
        ScheduleChannel channelSchedule = queryExecutor.execute(query);
    
        verify(contentQueryExecutor).resolveUris(argThat(hasItems(item.getCanonicalUri())), argThat(any(List.class)), argThat(any(Set.class)), eq(false));
        
        assertThat(channelSchedule.channel(), is(channel));
        
        List<Item> items = channelSchedule.items();
        assertThat(items.size(), is(2));
        
        Item firstItem = items.get(0);
        assertThat(firstItem.getCanonicalUri(), is(item.getCanonicalUri()));
        Version scheduleVersion = Iterables.getOnlyElement(firstItem.getVersions());
        Broadcast scheduleBroadcast = Iterables.getOnlyElement(scheduleVersion.getBroadcasts());
        assertThat(scheduleBroadcast.getSourceId(), is("bid"));

        Item secondItem = items.get(1);
        assertThat(secondItem.getCanonicalUri(), is(item.getCanonicalUri()));
        scheduleVersion = Iterables.getOnlyElement(secondItem.getVersions());
        scheduleBroadcast = Iterables.getOnlyElement(scheduleVersion.getBroadcasts());
        assertThat(scheduleBroadcast.getSourceId(), is("bid2"));
        
    }

    private void addBroadcast(Item item, String channelUri, DateTime start, DateTime end, String bid) {
        Broadcast broadcast = new Broadcast(channelUri, start, end);
        broadcast.withId(bid);
        
        Version version = Iterables.getFirst(item.getVersions(), new Version());
        version.addBroadcast(broadcast);
    }

    private EquivalentContent queryResult(String itemUri, List<Content> content) {
        Builder builder = EquivalentContent.builder();
        builder.putEquivalents(itemUri,content);
        return builder.build();
    }

    private Item itemWithBroadcast(String itemUri, String channelUri, DateTime start, DateTime end, String bid) {
        
        Broadcast broadcast = new Broadcast(channelUri, start, end);
        broadcast.withId(bid);
        
        Version version = new Version();
        version.addBroadcast(broadcast);
        
        Item item = new Item(itemUri, itemUri, Publisher.METABROADCAST);
        item.addVersion(version);
        
        return item;
    }
    
    private DateTime dateTime(int ts) {
        return new DateTime(ts, DateTimeZones.UTC);
    }
}
