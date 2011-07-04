package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.DayRangeGenerator;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonDateRangeScheduleUpdater extends ScheduledTask {
    
    public static final String SCHEDULE_PATTERN = "http://www.bbc.co.uk/iplayer/ion/schedule/service/%s/date/%s/timeslot/day/format/json";
    private static final int THREADS = 5;

    private final DayRangeGenerator dayRange;
    private final ContentResolver localFetcher;
    private final ContentWriter writer;
    private final AdapterLog log;

    private ItemsPeopleWriter itemsPeopleWriter;
    private BbcItemFetcherClient fetcherClient;
    private SimpleHttpClient httpClient = HttpClients.webserviceClient();
    private final Clock clock;

    public BbcIonDateRangeScheduleUpdater(DayRangeGenerator dayRange, ContentResolver localFetcher, ContentWriter writer, AdapterLog log, Clock clock) {
        this.dayRange = dayRange;
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.log = log;
        this.clock = clock;
    }
    
    public BbcIonDateRangeScheduleUpdater(DayRangeGenerator dayRange, ContentResolver localFetcher, ContentWriter writer, AdapterLog log) {
        this(dayRange, localFetcher, writer, log, new SystemClock());
    }

    public BbcIonDateRangeScheduleUpdater withItemFetchClient(BbcItemFetcherClient fetcherClient) {
        this.fetcherClient = fetcherClient;
        return this;
    }

    public BbcIonDateRangeScheduleUpdater withHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }
    
    public BbcIonDateRangeScheduleUpdater withItemsPeopleWriter(ItemsPeopleWriter itemsPeopleWriter) {
        this.itemsPeopleWriter = itemsPeopleWriter;
        return this;
    }

    @Override
    public void runTask() {
        DateTime start = new DateTime(DateTimeZones.UTC);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update initiated"));

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        
        
        for (String serviceKey : BbcIonServices.services.keySet()) {
            for (LocalDate day : dayRange.generate(clock.now().toLocalDate())) {
                
                if (!shouldContinue()) {
                    break;
                }
                reportStatus(String.format("%s - %s", serviceKey, day.toString("yyyy-MM-dd")));
                
                BbcIonScheduleUpdateTask updateTask = updateTaskFor(serviceKey, day);
                executor.submit(updateTask);
                
            }
        }
        
        executor.shutdown();
        boolean completion = false;
        try {
            completion = executor.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update interrupted waiting for completion").withCause(e));
        }
        
        String runTime = new Period(start, new DateTime(DateTimeZones.UTC)).toString(PeriodFormat.getDefault());
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update finished in " + runTime + (completion ? "" : " (timed-out)")));
    }

    private BbcIonScheduleUpdateTask updateTaskFor(String serviceKey, LocalDate day) {
        return new BbcIonScheduleUpdateTask(serviceKey, day, this.httpClient, localFetcher, writer, log)
            .withItemFetcherClient(fetcherClient)
            .withItemPeopleWriter(itemsPeopleWriter);
    }
}
