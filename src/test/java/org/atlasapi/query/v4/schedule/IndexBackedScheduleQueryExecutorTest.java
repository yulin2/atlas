package org.atlasapi.query.v4.schedule;

import static org.atlasapi.media.entity.MediaType.VIDEO;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.METABROADCAST;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.util.Resolved;
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

@RunWith(MockitoJUnitRunner.class)
public class IndexBackedScheduleQueryExecutorTest {

    private final ScheduleIndex index = mock(ScheduleIndex.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final IndexBackedScheduleQueryExecutor queryExecutor = new IndexBackedScheduleQueryExecutor(index, contentResolver);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDoesntResolveContentOnEmptyScheduleRef() throws Exception {
        Channel channel = new Channel(BBC, "One", "one", VIDEO, "one");
        Interval interval = new Interval(0, 100, DateTimeZones.UTC);
        ScheduleQuery query = new ScheduleQuery(METABROADCAST, channel, interval);

        when(index.resolveSchedule(METABROADCAST, channel, interval))
            .thenReturn(Futures.immediateFuture(ScheduleRef.forChannel(channel.getCanonicalUri()).build()));
        
        ChannelSchedule channelSchedule = queryExecutor.execute(query).getChannelSchedule();

        verify(contentResolver, never()).resolveIds(argThat(any(Iterable.class)));
        
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
                    .addEntry(new ScheduleRefEntry(item.getId().longValue(), channel.getCanonicalUri(), dateTime(25), dateTime(75), "bid"))
                    .build()
            ));
        
        when(contentResolver.resolveIds(argThat(hasItem(item.getId()))))
            .thenReturn(queryResult(ImmutableList.<Content>of(item)));
        
        ChannelSchedule channelSchedule = queryExecutor.execute(query).getChannelSchedule();

        verify(contentResolver).resolveIds(argThat(hasItems(item.getId())));
        
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
                    .addEntry(new ScheduleRefEntry(item.getId().longValue(), channel.getCanonicalUri(), dateTime(25), dateTime(75), "bid"))
                    .addEntry(new ScheduleRefEntry(item.getId().longValue(), channel.getCanonicalUri(), dateTime(125), dateTime(175), "bid2"))
                    .build()
            ));
        
        when(contentResolver.resolveIds(argThat(hasItems(item.getId()))))
            .thenReturn(queryResult(ImmutableList.<Content>of(item)));
    
        ChannelSchedule channelSchedule = queryExecutor.execute(query).getChannelSchedule();
    
        verify(contentResolver).resolveIds(argThat(hasItems(item.getId())));
        
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

    private Resolved<Content> queryResult(List<Content> content) {
        return Resolved.valueOf(content);
    }

    private Item itemWithBroadcast(String itemUri, String channelUri, DateTime start, DateTime end, String bid) {
        
        Broadcast broadcast = new Broadcast(channelUri, start, end);
        broadcast.withId(bid);
        
        Version version = new Version();
        version.addBroadcast(broadcast);
        
        Item item = new Item(itemUri, itemUri, Publisher.METABROADCAST);
        item.setId(Id.valueOf(Long.valueOf(itemUri.hashCode())));
        item.addVersion(version);
        
        return item;
    }
    
    private DateTime dateTime(int ts) {
        return new DateTime(ts, DateTimeZones.UTC);
    }
}
