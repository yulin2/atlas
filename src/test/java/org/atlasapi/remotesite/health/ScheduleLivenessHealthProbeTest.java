package org.atlasapi.remotesite.health;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.content.schedule.ScheduleIndex;
import org.atlasapi.media.content.schedule.ScheduleRef;
import org.atlasapi.media.content.schedule.ScheduleRef.ScheduleRefEntry;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.metabroadcast.common.health.ProbeResult;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.mockito.ArgumentCaptor;

public class ScheduleLivenessHealthProbeTest extends TestCase {
    
	private static final int TWELVE_POINT_FIVE_DAYS_IN_HOURS = 300;
	private DateTime startTimeForScheduleTest;
	private DateTime endTimeForScheduleTest;
	
    private static final Channel BBC_ONE = new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", false, MediaType.AUDIO, "http://www.bbc.co.uk/bbcone");
    private static final Channel BBC_TWO = new Channel(Publisher.METABROADCAST, "BBC Two", "bbctwo", false, MediaType.AUDIO, "http://www.bbc.co.uk/bbctwo");
    private static final Channel ITV1_LONDON = new Channel(Publisher.METABROADCAST, "ITV 1 London", "itv1london", false, MediaType.AUDIO, "http://www.itv.com");

	public void setUp() {
		this.startTimeForScheduleTest = new DateTime().plusHours(TWELVE_POINT_FIVE_DAYS_IN_HOURS);
		this.endTimeForScheduleTest = startTimeForScheduleTest.plusHours(12);
	}
	
	public void testHasSchedule() throws Exception {
		
		Interval dummyInterval = new Interval(startTimeForScheduleTest, endTimeForScheduleTest);
		Item item = new Item();
		item.setId(1);
		
		ScheduleIndex scheduleIndex = mock(ScheduleIndex.class);
		
		when(scheduleIndex.resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_ONE)), argThat(is(any(Interval.class)))))
		    .thenReturn(Futures.immediateFuture(ScheduleRef.forChannel(BBC_ONE.getCanonicalUri())
	            .addEntry(new ScheduleRefEntry(item.getId().longValue(), BBC_ONE.getCanonicalUri(), dummyInterval.getStart(), dummyInterval.getEnd(), null))
	            .build()
            ));
		when(scheduleIndex.resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_TWO)), argThat(is(any(Interval.class)))))
		.thenReturn(Futures.immediateFuture(ScheduleRef.forChannel(BBC_TWO.getCanonicalUri())
		        .addEntry(new ScheduleRefEntry(item.getId().longValue(), BBC_TWO.getCanonicalUri(), dummyInterval.getStart(), dummyInterval.getEnd(), null))
		        .build()
	        ));
		
		ScheduleLivenessHealthProbe probe = new ScheduleLivenessHealthProbe(scheduleIndex,
				ImmutableList.of(BBC_ONE, BBC_TWO), Publisher.PA);
		ProbeResult result = probe.probe();
		
		ArgumentCaptor<Interval> interval = ArgumentCaptor.forClass(Interval.class);
		verify(scheduleIndex).resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_ONE)), interval.capture());
		
		assertThat(result.isFailure(), is(false));
		assertThat(Minutes.minutesBetween(interval.getValue().getStart(), startTimeForScheduleTest), lessThan((BaseSingleFieldPeriod) Minutes.minutes(2)));
		assertThat(Minutes.minutesBetween(interval.getValue().getEnd(), endTimeForScheduleTest), lessThan((BaseSingleFieldPeriod) Minutes.minutes(2)));
	}
	
	public void testNoSchedule() throws Exception {
		Interval dummyInterval = new Interval(new DateTime().plusHours(TWELVE_POINT_FIVE_DAYS_IN_HOURS), new DateTime().plusHours(TWELVE_POINT_FIVE_DAYS_IN_HOURS).plusHours(12));
		Item item = new Item();
		item.setId(1);
		
		ScheduleIndex scheduleIndex = mock(ScheduleIndex.class);
        
        when(scheduleIndex.resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_ONE)), argThat(is(any(Interval.class)))))
            .thenReturn(Futures.immediateFuture(ScheduleRef.forChannel(BBC_ONE.getCanonicalUri())
                .addEntry(new ScheduleRefEntry(item.getId().longValue(), BBC_ONE.getCanonicalUri(), dummyInterval.getStart(), dummyInterval.getEnd(), null))
                .build()
            ));
        when(scheduleIndex.resolveSchedule(argThat(is(Publisher.PA)), argThat(is(ITV1_LONDON)), argThat(is(any(Interval.class)))))
            .thenReturn(Futures.<ScheduleRef>immediateFuture(null));
        

        ScheduleLivenessHealthProbe probe = new ScheduleLivenessHealthProbe(scheduleIndex,
				ImmutableList.of(BBC_ONE, ITV1_LONDON), Publisher.PA);
		ProbeResult result = probe.probe();
		
		ArgumentCaptor<Interval> interval = ArgumentCaptor.forClass(Interval.class);
        verify(scheduleIndex).resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_ONE)), interval.capture());
        
        assertThat(result.isFailure(), is(true));
        assertThat(Minutes.minutesBetween(interval.getValue().getStart(), startTimeForScheduleTest), lessThan((BaseSingleFieldPeriod) Minutes.minutes(2)));
        assertThat(Minutes.minutesBetween(interval.getValue().getEnd(), endTimeForScheduleTest), lessThan((BaseSingleFieldPeriod) Minutes.minutes(2)));
		
	}
	
	public void testNoItemsInSchedule() throws Exception {
		Interval dummyInterval = new Interval(new DateTime().plusHours(TWELVE_POINT_FIVE_DAYS_IN_HOURS), new DateTime().plusHours(TWELVE_POINT_FIVE_DAYS_IN_HOURS).plusHours(12));
		Item item = new Item();
		item.setId(1);

        ScheduleIndex scheduleIndex = mock(ScheduleIndex.class);

        when(scheduleIndex.resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_ONE)), argThat(is(any(Interval.class)))))
        .thenReturn(Futures.immediateFuture(ScheduleRef
            .forChannel(BBC_ONE.getCanonicalUri())
            .addEntry(new ScheduleRefEntry(item.getId().longValue(), BBC_ONE.getCanonicalUri(), dummyInterval.getStart(), dummyInterval.getEnd(), null))
            .build()
        ));
        
        when(scheduleIndex.resolveSchedule(argThat(is(Publisher.PA)), argThat(is(ITV1_LONDON)), argThat(is(any(Interval.class)))))
        .thenReturn(Futures.immediateFuture(ScheduleRef
            .forChannel(ITV1_LONDON.getCanonicalUri())
            .build()
        ));
        
		ScheduleLivenessHealthProbe probe = new ScheduleLivenessHealthProbe(scheduleIndex,
				ImmutableList.of(BBC_ONE, ITV1_LONDON), Publisher.PA);
		
		ProbeResult result = probe.probe();
		
		ArgumentCaptor<Interval> interval = ArgumentCaptor.forClass(Interval.class);
        verify(scheduleIndex).resolveSchedule(argThat(is(Publisher.PA)), argThat(is(BBC_ONE)), interval.capture());
		
        assertThat(result.isFailure(), is(true));
        assertThat(Minutes.minutesBetween(interval.getValue().getStart(), startTimeForScheduleTest), lessThan((BaseSingleFieldPeriod) Minutes.minutes(2)));
        assertThat(Minutes.minutesBetween(interval.getValue().getEnd(), endTimeForScheduleTest), lessThan((BaseSingleFieldPeriod) Minutes.minutes(2)));
    	}
	
	protected static class DummyScheduleResolver implements ScheduleResolver {

		public DateTime requestedStartTime;
		public DateTime requestedEndTime;
		
		private Schedule schedule;
		
		public DummyScheduleResolver(Schedule dummySchedule) {
			this.schedule = dummySchedule;
		}

		@Override
		public Schedule schedule(DateTime from, DateTime to,
				Iterable<Channel> channels, Iterable<Publisher> publisher, Optional<ApplicationConfiguration> mergeConfig) {
			
			this.requestedStartTime = from;
			this.requestedEndTime = to;
			
			final List<Channel> channelList = ImmutableList.copyOf(channels);
			
			return new Schedule(ImmutableList.copyOf(Iterables.filter(schedule.channelSchedules(), new Predicate<ChannelSchedule>() {

				@Override
				public boolean apply(ChannelSchedule input) {
					return channelList.contains(input.getChannel());
				}
				
				
			})), schedule.interval());
			
		}
		
	}
}
