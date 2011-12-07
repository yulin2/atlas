package org.atlasapi.remotesite.bbc.ion.ondemand;

import static org.atlasapi.persistence.logging.AdapterLogEntry.debugEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonOndemandChangeUpdateBuilder {

    private static final int MAX_FORWARD_STEPS = 1000;
    private static final int MAX_RETRIES = 3;
    
    private final static String CHANGES_URL = "http://www.bbc.co.uk/iplayer/ion/ondemand/change/from_datetime/%S/format/json";

    private final AdapterLog log;
    private final RemoteSiteClient<IonOndemandChanges> feedClient;
    private final ExecutorService updateProcessor;
    private final BbcIonOndemandChangeTaskBuilder taskBuilder;

    public BbcIonOndemandChangeUpdateBuilder(BbcIonOndemandChangeTaskBuilder taskBuilder, AdapterLog log, RemoteSiteClient<IonOndemandChanges> feedClient) {
        this.taskBuilder = taskBuilder;
        this.log = log;
        this.feedClient = feedClient;
        this.updateProcessor = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("Ion Ondemand Update Processor").build());
    }
    
    public BbcIonOndemandChangeUpdate updateStartingFrom(DateTime start) {
        return new BbcIonOndemandChangeUpdate(start);
    }

    public class BbcIonOndemandChangeUpdate {

        private final DateTime start;

        public BbcIonOndemandChangeUpdate(DateTime start) {
            this.start = start;
        }

        public DateTime runUpdate() {
            CompletionService<Void> completer = new ExecutorCompletionService<Void>(updateProcessor);
            
            DateTime fromDateTime = start;
                
            Set<Future<Void>> tasks = Sets.newHashSet();

            int total = 0;
            
            try {
                for (int i = 0; i < MAX_FORWARD_STEPS; i++) {

                    DateTime utcStartTime = fromDateTime.toDateTime(DateTimeZones.UTC);
                    log.record(debugEntry().withSource(getClass()).withDescription("Fetching ondemand change tasks from %s. ", utcStartTime));
                    
                    IonOndemandChanges changes = fetch(fromDateTime);

                    for (IonOndemandChange change : changes.getBlocklist()) {
                        tasks.add(completer.submit(taskBuilder.taskForChange(change)));
                    }
                    
                    int changeCount = changes.getBlocklist().size();
                    total += changeCount;
                    log.record(debugEntry().withSource(getClass()).withDescription("%s ondemand change tasks from %s. %s total", changeCount, utcStartTime, total));

                    if (changes.getNextFromDatetime() == null || fromDateTime.equals(changes.getNextFromDatetime())) {
                        break;
                    }
                    fromDateTime = changes.getNextFromDatetime();
                }
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Unable to fetch ondemand changes for " + String.format(CHANGES_URL, fromDateTime.toString())));
            }

            int done = 0;
            try {
                for (int t = 1; t <= tasks.size(); t++) {
                    completer.take();
                    done++;
                    if (t % 20 == 0) {
                        log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Completed %d ondemand change tasks", t)));
                    }
                }
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Exception processing ondemand change"));
            }
            log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Completed %s ondemand change tasks", done)));

            return fromDateTime;
        }
        
        private IonOndemandChanges fetch(DateTime limitedLastRun) throws Exception {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    return feedClient.get(String.format(CHANGES_URL, limitedLastRun.toString()));
                } catch (Exception e) {
                    
                    // throw if no more retries
                    if (i == (MAX_RETRIES - 1)) {
                        throw e;
                    }
                    // ignore and try again
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {}
                }
            }
            // should never reach here
            throw new IllegalStateException();
        }
    }
    
}
