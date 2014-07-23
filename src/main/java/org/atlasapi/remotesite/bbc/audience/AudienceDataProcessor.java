package org.atlasapi.remotesite.bbc.audience;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.AudienceStatistics;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Demographic;
import org.atlasapi.media.entity.DemographicSegment;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Rating;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class AudienceDataProcessor extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(AudienceDataProcessor.class);
    
    private final long FIVE_MINUTES_IN_SECONDS = Duration.standardMinutes(5).getStandardSeconds();
    private final ScheduleResolver scheduleResolver;
    private final ChannelResolver channelResolver;
    private final AudienceDataReader audienceDataReader;
    private final ContentWriter contentWriter;
    
    public AudienceDataProcessor(ScheduleResolver scheduleResolver,
            ChannelResolver channelResolver, ContentWriter contentWriter,
            AudienceDataReader audienceDataReader) {
        this.scheduleResolver = checkNotNull(scheduleResolver);
        this.channelResolver = checkNotNull(channelResolver);
        this.contentWriter = checkNotNull(contentWriter);
        this.audienceDataReader = checkNotNull(audienceDataReader);
    }
    
    @Override
    protected void runTask() {
        try {
            process(audienceDataReader.readData());
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }
    
    public void process(Iterable<AudienceDataRow> rows) {
        Multimap<LookupRef, AudienceDataRow> itemMap = HashMultimap.create();
        String lastRowChannel = null;
        LocalDate lastRowDay = null;
        Schedule currentSchedule = null;
        for (AudienceDataRow row: rows) {
            LocalDate date = row.getDate();
            String channelKey = row.getChannel();
            LocalTime startTime = row.getStartTime();
            if (lastRowChannel == null 
                    || !lastRowChannel.equals(channelKey)
                    || lastRowDay == null
                    || !lastRowDay.equals(date)) {
                currentSchedule = getScheduleFor(channelKey, date, startTime);
            }
            
            List<Item> items = Iterables.getOnlyElement(currentSchedule.scheduleChannels()).items();
            
            boolean matched = false;
            for(Item item: items) {
                if (matches(item, row)) {
                    itemMap.put(topLevelRef(item), row);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                log.error("Didn't match " + row.getTitle());
            } 
        }
        writeEntries(itemMap);
    }
    
    private void writeEntries(Multimap<LookupRef, AudienceDataRow> itemMap) {
        for (LookupRef key : itemMap.keySet()) {
            Collection<AudienceDataRow> rows = itemMap.get(key);
            AudienceDataRow merged = mergeRows(rows);
            write(key, merged);
        }
    }
    
    private void write(LookupRef lookupRef, AudienceDataRow audienceData) {
        Item item = new Item(canonicalUriOfStatsFor(lookupRef.uri()), null, Publisher.BBC_AUDIENCE_STATS);
        item.setRatings(ratingsFrom(audienceData));
        item.setAudienceStatistics(audienceStatsFrom(audienceData));
        item.setEquivalentTo(ImmutableSet.of(lookupRef));
        contentWriter.createOrUpdate(item);
    }
    
    private AudienceStatistics audienceStatsFrom(AudienceDataRow audienceData) {
        
        return new AudienceStatistics(audienceData.getAudience().multiply(BigDecimal.valueOf(1000000)).longValue(), audienceData.getShare().floatValue(), 
                ImmutableSet.of(
                        genderDemographicsFor(audienceData),
                        socialGroupDemographicsFor(audienceData),
                        ageDemographicsFor(audienceData)
                        ));
    }

    private Demographic socialGroupDemographicsFor(AudienceDataRow audienceData) {
        return new Demographic("social-group", 
                ImmutableList.of(
                        new DemographicSegment("ab", "AB", audienceData.getAb()),
                        new DemographicSegment("c1", "C1", audienceData.getC1()),
                        new DemographicSegment("c2", "C2", audienceData.getC2()),
                        new DemographicSegment("de", "DE", audienceData.getDe())
                ));
    }

    private Demographic genderDemographicsFor(AudienceDataRow audienceData) {
        return new Demographic("gender", 
                ImmutableList.of(
                        new DemographicSegment("male", "Male", audienceData.getMale()),
                        new DemographicSegment("female", "Female", audienceData.getFemale())
                ));
                        
        
    }
    
    private Demographic ageDemographicsFor(AudienceDataRow audienceData) {
        return new Demographic("age", 
                ImmutableList.of(
                        new DemographicSegment("4to9", "4 to 9", audienceData.getAge4to9()),
                        new DemographicSegment("10to15", "10 to 15", audienceData.getAge10to15()),
                        new DemographicSegment("16to24", "16 to 24", audienceData.getAge16to24()),
                        new DemographicSegment("25to34", "25 to 34", audienceData.getAge25to34()),
                        new DemographicSegment("35to44", "35 to 44", audienceData.getAge35to44()),
                        new DemographicSegment("45to54", "45 to 54", audienceData.getAge45to54()),
                        new DemographicSegment("55to64", "55 to 64", audienceData.getAge55to64()),
                        new DemographicSegment("65plus", "65+", audienceData.getAge65plus())
                ));
                        
        
    }

    private Iterable<Rating> ratingsFrom(AudienceDataRow audienceData) {
        Integer ai = audienceData.getAi();
        
        if (ai == null) {
            return ImmutableSet.of();
        }
        
        Rating rating = new Rating("AI", ai, Publisher.BBC_AUDIENCE_STATS);
        return ImmutableSet.of(rating);

    }

    private String canonicalUriOfStatsFor(String uri) {
        return "http://" + Publisher.BBC_AUDIENCE_STATS.key() + "/" + uri.replace("http://", "");
    }
    private AudienceDataRow mergeRows(Iterable<AudienceDataRow> rows) {
        List<Integer> age4to9s = Lists.newArrayList();
        List<Integer> age10to15s = Lists.newArrayList();
        List<Integer> age16to24s = Lists.newArrayList();
        List<Integer> age25to34s = Lists.newArrayList();
        List<Integer> age35to44s = Lists.newArrayList();
        List<Integer> age45to54s = Lists.newArrayList();
        List<Integer> age55to64s = Lists.newArrayList();
        List<Integer> age65plus = Lists.newArrayList();
        List<Integer> male = Lists.newArrayList();
        List<Integer> female = Lists.newArrayList();
        List<Integer> ai = Lists.newArrayList();
        List<BigDecimal> audience = Lists.newArrayList();
        List<BigDecimal> share = Lists.newArrayList();
        List<Integer> ab = Lists.newArrayList();
        List<Integer> c1 = Lists.newArrayList();
        List<Integer> c2 = Lists.newArrayList();
        List<Integer> de = Lists.newArrayList();
        
        for (AudienceDataRow row : rows) {
            ai.add(row.getAi());
            male.add(row.getMale());
            female.add(row.getFemale());
            audience.add(row.getAudience());
            share.add(row.getShare());
            age4to9s.add(row.getAge4to9());
            age10to15s.add(row.getAge10to15());
            age16to24s.add(row.getAge16to24());
            age25to34s.add(row.getAge25to34());
            age35to44s.add(row.getAge35to44());
            age45to54s.add(row.getAge45to54());
            age55to64s.add(row.getAge55to64());
            age65plus.add(row.getAge65plus());
            ab.add(row.getAb());
            c1.add(row.getC1());
            c2.add(row.getC2());
            de.add(row.getDe());
        }
        
        return new AudienceDataRow(null, null, null, null, null, null, mean(audience), mean(share),
                meanInt(ai), meanInt(male), meanInt(female), null, meanInt(age4to9s), meanInt(age10to15s), meanInt(age16to24s),
                meanInt(age25to34s), meanInt(age35to44s), meanInt(age45to54s), meanInt(age55to64s), meanInt(age65plus),
                meanInt(ab), meanInt(c1), meanInt(c2), meanInt(de));
        
    }
    
    private BigDecimal mean(List<BigDecimal> values) {
        int count = 0;
        BigDecimal sum = BigDecimal.ZERO;
        
        for (BigDecimal value : values) {
            if (value != null) {
                count++;
                sum = sum.add(value);
            }
        }
        
        if (count == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count));
    }

    private Integer meanInt(List<Integer> values) {
        int count = 0;
        Integer sum = Integer.valueOf(0);
        
        for (Integer value : values) {
            if (value != null) {
                count++;
                sum += value;
            }
        }
        
        if (count == 0) {
            return null;
        }
        return sum / count;
    }

    private LookupRef topLevelRef(Item item) {
        if (item.getContainer() != null
                && item.getContainer().getUri() != null) {
            ParentRef container = item.getContainer();
            return new LookupRef(container.getUri(), container.getId(), item.getPublisher(), ContentCategory.CONTAINER);
        }
        return LookupRef.from(item);  
    }
    
    private boolean matches(Item item, AudienceDataRow row) {
        Broadcast broadcast = Iterables.getOnlyElement(Iterables.getOnlyElement(item.getVersions()).getBroadcasts());
        
        DateTime audienceStartTime = row.getDate().toDateTime(row.getStartTime(), DateTimeZone.forID("Europe/London"));
        DateTime audienceEndTime = row.getDate().toDateTime(row.getEndTime(), DateTimeZone.forID("Europe/London"));
        long audienceDuration = (audienceEndTime.getMillis() - audienceStartTime.getMillis()) / 1000;
        long diffBetweenTxStarts = Math.abs((broadcast.getTransmissionTime().getMillis() - audienceStartTime.getMillis()) / 1000);
        long diffBetweenDurations = Math.abs(broadcast.getBroadcastDuration() - audienceDuration);
        return (diffBetweenTxStarts < FIVE_MINUTES_IN_SECONDS
                && diffBetweenDurations < FIVE_MINUTES_IN_SECONDS);
    }

    private Schedule getScheduleFor(String channelKey, LocalDate date, LocalTime startTime) {
        Maybe<Channel> channel = channelResolver.fromKey(channelKey);
        DateTime from = date.toDateTimeAtStartOfDay();
        DateTime to = from.plusDays(1);
        return scheduleResolver.unmergedSchedule(from, to, 
                ImmutableSet.of(channel.requireValue()), ImmutableSet.of(Publisher.PA));
    }

}
