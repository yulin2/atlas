package org.atlasapi.remotesite.health;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.TimeMachine;

import junit.framework.TestCase;

public class ScheduleProbeTest extends TestCase {

    private final TimeMachine clock = new TimeMachine();
    private final Mockery context = new Mockery();
    private final KnownTypeQueryExecutor queryExecutor = context.mock(KnownTypeQueryExecutor.class);

    public void testProbeProducesFailureResultWhenNoEntriesInSchedule() throws Exception {

        clock.jumpTo(new DateTime(500, DateTimeZones.UTC));
        
        ScheduleProbe probe = new ScheduleProbe(Publisher.C4, Channel.CHANNEL_FOUR, queryExecutor, clock);
        
        context.checking(new Expectations(){{
            one(queryExecutor).schedule(with(any(ContentQuery.class)));
                will(returnValue(Schedule.fromItems(ImmutableList.of(Channel.CHANNEL_FOUR.uri()), dayIntervalAround(clock.now()), ImmutableList.<Item>of())));
        }});
        
        ProbeResult result = probe.probe();
        
        assertThat(result.isFailure(), is(true));
        assertThat(Iterables.size(result.entries()), is(1));
    }
    
    public void testScheduleWithNoGapsPasses() throws Exception {
        
        clock.jumpTo(new DateTime(500, DateTimeZones.UTC));
        
        ScheduleProbe probe = new ScheduleProbe(Publisher.C4, Channel.CHANNEL_FOUR, queryExecutor, clock);
        
                                    //start, duration in ms
        final Item item1 = broadcastItem(10, 10);
        final Item item2 = broadcastItem(22, 10);
        
        context.checking(new Expectations(){{
            one(queryExecutor).schedule(with(any(ContentQuery.class)));
                will(returnValue(Schedule.fromItems(dayIntervalAround(clock.now()), ImmutableList.<Item>of(item1, item2))));
        }});
        
        ProbeResult result = probe.probe();
        
        assertThat(result.isFailure(), is(false));
    }
    
    public void testScheduleWithGapsFails() throws Exception {
        
        clock.jumpTo(new DateTime(500, DateTimeZones.UTC));
        
        ScheduleProbe probe = new ScheduleProbe(Publisher.C4, Channel.CHANNEL_FOUR, queryExecutor, clock);
        
        final Item item1 = broadcastItem(10, 10);
        final Item item2 = broadcastItem(300022, 10); //starts 5mins and 2 millis after
        
        context.checking(new Expectations(){{
            one(queryExecutor).schedule(with(any(ContentQuery.class)));
                will(returnValue(Schedule.fromItems(dayIntervalAround(clock.now()), ImmutableList.<Item>of(item1, item2))));
        }});
        
        ProbeResult result = probe.probe();
        
        assertThat(result.isFailure(), is(true));
        assertThat(Iterables.get(result.entries(),0).isFailure(), is(false));
        assertThat(Iterables.get(result.entries(),1).isFailure(), is(true));
        assertThat(Iterables.get(result.entries(),2).isFailure(), is(false)); 
        assertThat(Iterables.get(result.entries(),3).isFailure(), is(false)); 
    }
    
    public void testScheduleWithOverlapsFails() throws Exception {
        
        clock.jumpTo(new DateTime(500, DateTimeZones.UTC));
        
        ScheduleProbe probe = new ScheduleProbe(Publisher.C4, Channel.CHANNEL_FOUR, queryExecutor, clock);
        
        final Item item1 = broadcastItem(10, 10);
        final Item item2 = broadcastItem(18, 10); //previous ends at 20
        
        context.checking(new Expectations(){{
            one(queryExecutor).schedule(with(any(ContentQuery.class)));
            will(returnValue(Schedule.fromItems(dayIntervalAround(clock.now()), ImmutableList.<Item>of(item1, item2))));
        }});
        
        ProbeResult result = probe.probe();
        
        assertThat(result.isFailure(), is(true));
        assertThat(Iterables.get(result.entries(),0).isFailure(), is(false));
        assertThat(Iterables.get(result.entries(),1).isFailure(), is(false));
        assertThat(Iterables.get(result.entries(),2).isFailure(), is(true)); 
        assertThat(Iterables.get(result.entries(),3).isFailure(), is(false)); 
    }
    
    public void testScheduleWithStaleItemsFails() throws Exception {
        
        clock.jumpTo(new DateTime(3600020, DateTimeZones.UTC));
        
        ScheduleProbe probe = new ScheduleProbe(Publisher.C4, Channel.CHANNEL_FOUR, queryExecutor, clock);
        
        final Item item1 = broadcastItem(10, 10, 10);
        final Item item2 = broadcastItem(22, 10, 10);
        
        context.checking(new Expectations(){{
            one(queryExecutor).schedule(with(any(ContentQuery.class)));
                will(returnValue(Schedule.fromItems(dayIntervalAround(clock.now()), ImmutableList.<Item>of(item1, item2))));
        }});
        
        ProbeResult result = probe.probe();
        
        assertThat(result.isFailure(), is(true));
        assertThat(Iterables.get(result.entries(),0).isFailure(), is(false));
        assertThat(Iterables.get(result.entries(),1).isFailure(), is(false));
        assertThat(Iterables.get(result.entries(),2).isFailure(), is(false));
        assertThat(Iterables.get(result.entries(),3).isFailure(), is(true)); 
    }
    
    private Item broadcastItem(long start, long duration) {
        return broadcastItem(start, duration, start);
    }
        
    private Item broadcastItem(long start, long duration, long lastFetched) {
        Item item = new Item("item"+start, "curie"+start, Publisher.C4);
        item.setLastFetched(new DateTime(lastFetched, DateTimeZones.UTC));
        
        Version version = new Version();
        
        Broadcast broadcast = new Broadcast(Channel.CHANNEL_FOUR.uri(), new DateTime(start, DateTimeZones.UTC), new Duration(duration));
        
        version.addBroadcast(broadcast);
        item.addVersion(version);
        
        return item;
    }

    private Interval dayIntervalAround(DateTime now) {
        DateTime start = now.toLocalDate().toDateTimeAtStartOfDay(DateTimeZones.UTC);
        return new Interval(start, start.plusDays(1));
    }

}
