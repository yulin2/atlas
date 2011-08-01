package org.atlasapi.remotesite.bbc.ion;

import static com.metabroadcast.common.time.DateTimeZones.UTC;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.bbc.BbcIonScheduleClient;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.DayRangeGenerator;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonDateRangeScheduleUpdater extends ScheduledTask {

    public static final String SCHEDULE_PATTERN = "http://www.bbc.co.uk/iplayer/ion/schedule/service/%s/date/%s/timeslot/day/format/json";
    private static final int THREADS = 5;

    private final DayRangeGenerator dayRange;
    private final BbcIonScheduleHandler handler;
    private final AdapterLog log;

    private BbcIonScheduleClient scheduleClient = new BbcIonScheduleClient(SCHEDULE_PATTERN);
    private final Clock clock;

    public BbcIonDateRangeScheduleUpdater(DayRangeGenerator dayRange, BbcIonScheduleHandler handler, AdapterLog log, Clock clock) {
        this.dayRange = dayRange;
        this.handler = handler;
        this.log = log;
        this.clock = clock;
    }

    public BbcIonDateRangeScheduleUpdater(DayRangeGenerator dayRange, BbcIonScheduleHandler handler, AdapterLog log) {
        this(dayRange, handler, log, new SystemClock());
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
                completer.submit(new BbcIonScheduleUpdateTask(serviceKey, day.toDateTimeAtStartOfDay(UTC), scheduleClient, handler, log));
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
}
