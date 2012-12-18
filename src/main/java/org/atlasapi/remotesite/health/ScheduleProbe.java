package org.atlasapi.remotesite.health;

import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.FAILURE;
import static com.metabroadcast.common.health.ProbeResult.ProbeResultType.INFO;

import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.ScheduleEntry;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.health.ProbeResult.ProbeResultEntry;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;

public class ScheduleProbe implements HealthProbe {

    private static final Duration MAX_GAP = Duration.standardMinutes(5);
    
    private final Publisher publisher;
    private final Channel channel;
    private final ScheduleResolver scheduleResolver;
    private final Clock clock;
    private final int days;
    
    public ScheduleProbe(Publisher publisher, Channel channel, ScheduleResolver scheduleResolver, Clock clock) {
        this(publisher, channel, scheduleResolver, clock, 7);
    }

    public ScheduleProbe(Publisher publisher, Channel channel, ScheduleResolver scheduleResolver, Clock clock, int days) {
        this.publisher = publisher;
        this.channel = channel;
        this.scheduleResolver = scheduleResolver;
        this.clock = clock;
        this.days = days;
    }

    @Override
    public ProbeResult probe() throws Exception {
        ProbeResult result = new ProbeResult(title());
        if (channel == null) {
            result.addInfo("Channel", "not found");
            return result;
        }
        
        LocalDate date = clock.now().toDateTime(DateTimeZones.LONDON).toLocalDate();
        
        for (int i = 0; i < days; i++, date = date.plusDays(1)) {
            DateTime start = date.toDateTimeAtStartOfDay();
            Schedule schedule = scheduleResolver.schedule(start, start.plusDays(1), ImmutableSet.of(channel), ImmutableSet.of(publisher), null);
            result.addEntry(scheduleSize(schedule));
            addContiguityEntries(schedule, result);
        }

        return result;
    }

    private ProbeResultEntry scheduleSize(Schedule schedule) {
        int scheduleSize = Iterables.getOnlyElement(schedule.scheduleChannels()).items().size();
        return new ProbeResultEntry(scheduleSize > 0 ? INFO : FAILURE, startString(schedule) + " entries", String.valueOf(scheduleSize));
    }

    private String startString(Schedule schedule) {
        return schedule.interval().getStart().toString("dd/MM/yyyy");
    }

    private void addContiguityEntries(Schedule schedule, ProbeResult result) {
        List<Item> items = Iterables.getOnlyElement(schedule.scheduleChannels()).items();
        
        if(items.isEmpty()) {
            return;
        }

        int breaks = 0, overlaps = 0;
        DateTime lastEnd = ScheduleEntry.BROADCAST.apply(items.get(0)).getTransmissionTime();

        for (Item item: items) {
            Broadcast broadcast = ScheduleEntry.BROADCAST.apply(item);
            DateTime transmissionStart = broadcast.getTransmissionTime();
            
            if(transmissionStart.isAfter(lastEnd.plus(MAX_GAP))) {
                breaks++;
            } else if (transmissionStart.isBefore(lastEnd)) {
                overlaps++;
            }
            
            lastEnd = broadcast.getTransmissionEndTime();
        }

        String date = startString(schedule);
        result.add(date + " gaps", String.valueOf(breaks), !(breaks > 0));
        result.add(date + " overlaps", String.valueOf(overlaps), !(overlaps > 0));
    }

    @Override
    public String title() {
        return String.format("Schedule %s: %s", publisher.title(), channel == null ? "Unknown Channel" : channel.title());
    }

    @Override
    public String slug() {
        return String.format("%s%sschedule", publisher.name(), channel == null ? 0 : channel.key());
    }
}
