package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    private SimpleHttpClient httpClient = HttpClients.webserviceClient();

    private BbcItemFetcherClient itemClient;
    private BbcContainerFetcherClient containerClient;
    private ItemsPeopleWriter itemsPeopleWriter;
    
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

    public BbcIonDateRangeScheduleUpdater withItemFetchClient(BbcItemFetcherClient itemClient) {
        this.itemClient = itemClient;
        return this;
    }
    
    public BbcIonDateRangeScheduleUpdater withContainerFetchClient(BbcContainerFetcherClient containerClient) {
        this.containerClient = containerClient;
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
        CompletionService<Integer> completer = new ExecutorCompletionService<Integer>(executor);
        
        int submitted = 0;
        
        for (String serviceKey : BbcIonServices.services.keySet()) {
            for (LocalDate day : dayRange.generate(clock.now().toLocalDate())) {
                completer.submit(updateTaskFor(serviceKey, day));
                submitted++;
            }
        }
        reportStatus(String.format("Submitted %s update tasks", submitted));
        
        int processed = 0, failed = 0, broadcasts = 0;
        
        for (int i = 0; i < submitted; i++) {
            try {
                if (!shouldContinue()) {
                    break;
                }
                Future<Integer> result = completer.poll(5, TimeUnit.SECONDS);
                if (result != null) {
                    try {
                        broadcasts += result.get();
                    } catch (Exception e) {
                        failed++;
                    }
                }
                reportStatus(String.format("Processed %s / %s. %s failures. %s broadcasts processed", ++processed, submitted, failed, broadcasts));
            } catch (InterruptedException e) {
                log.record(AdapterLogEntry.warnEntry().withCause(e).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update interrupted waiting for results"));
            }
        }
        
        executor.shutdown();
        
        String runTime = new Period(start, new DateTime(DateTimeZones.UTC)).toString(PeriodFormat.getDefault());
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update finished in " + runTime));
    }

    private BbcIonScheduleUpdateTask updateTaskFor(String serviceKey, LocalDate day) {
        return new BbcIonScheduleUpdateTask(serviceKey, day, this.httpClient, localFetcher, writer, log)
            .withItemFetcherClient(itemClient)
            .withContainerFetcherClient(containerClient)
            .withItemPeopleWriter(itemsPeopleWriter);
    }
}
