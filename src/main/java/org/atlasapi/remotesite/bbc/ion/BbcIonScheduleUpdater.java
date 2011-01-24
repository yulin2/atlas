package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

public class BbcIonScheduleUpdater implements Runnable {

    private final Iterable<String> uriSource;
    private final ExecutorService executor;
    private final AdapterLog log;

    public BbcIonScheduleUpdater(Iterable<String> uriSource, AdapterLog log) {
        this.uriSource = uriSource;
        this.log = log;
        executor = Executors.newFixedThreadPool(3);
    }
    
    @Override
    public void run() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update initiated"));
        for (String uri : uriSource) {
            executor.submit(BbcIonScheduleUpdateTask.forUri(uri));
        }
        executor.shutdown();
        boolean completion = false;
        try {
            completion = executor.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("BBC Ion Schedule Update interrupted waiting for completion").withCause(e));
        }
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update finished (" + (completion ? "normally)" : "timed-out)")));
    }

}
