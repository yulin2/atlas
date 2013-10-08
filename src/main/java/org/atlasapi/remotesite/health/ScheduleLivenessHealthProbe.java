package org.atlasapi.remotesite.health;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.content.schedule.ScheduleIndex;
import org.atlasapi.media.content.schedule.ScheduleRef;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;

public class ScheduleLivenessHealthProbe implements HealthProbe {

	private ScheduleIndex scheduleIndex;
	private Set<Channel> channels;
	
	private static final int TWELVE_POINT_FIVE_DAYS_IN_HOURS = 300;
	public static final String SCHEDULE_HEALTH_PROBE_SLUG = "schedule-liveness";
	
	private final Log log = LogFactory.getLog(getClass());
	private Publisher publisher;
	
	public ScheduleLivenessHealthProbe(ScheduleIndex scheduleIndex, Iterable<Channel> channels, Publisher publisher) {
		this.scheduleIndex = scheduleIndex;
		this.channels = ImmutableSet.copyOf(channels);
		this.publisher = publisher;
	}

	@Override
	public ProbeResult probe() throws Exception {
	    
		ProbeResult result = null;
		try {
			result = new ProbeResult(title());
			if (channels.isEmpty()) {
			    result.addInfo("Channels", "not configured");
			}
			
			DateTime startTime = new DateTime().plusHours(TWELVE_POINT_FIVE_DAYS_IN_HOURS);
			DateTime endTime = startTime.plusHours(12);
			
			for(Channel channel : channels) {
				try {
					ScheduleRef schedule = scheduleIndex.resolveSchedule(publisher, channel, new Interval(startTime, endTime)).get(1, TimeUnit.MINUTES);
					
					int itemCount = schedule == null ? 0 : schedule.getScheduleEntries().size();
							
					result.add(channel.title(), String.format("%d items in schedule from %s to %s", itemCount, startTime.toString("dd/MM/yy HH:mm"), endTime.toString("dd/MM/yy HH:mm")), itemCount > 0);
				}
				catch(Exception e) {
					result.add(channel.title(), "Exception processing channel", false);
					log.error("Exception processing channel " + channel.title(), e);
				}
			}
		}
		catch(Exception e) {
			log.error("Exception in schedule liveness probe", e);
			Throwables.propagate(e);
		}
		
		return result;
	}

	@Override
	public String title() {
		return "Schedule liveness";
	}

	@Override
	public String slug() {
		return SCHEDULE_HEALTH_PROBE_SLUG;
	}

}
