package org.atlasapi.remotesite.bbc.ion;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
import static org.atlasapi.remotesite.HttpClients.ATLAS_USER_AGENT;
import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonOndemandChangeUpdateBuilder {

    private static final int MAX_FORWARD_STEPS = 1000;
    private static final int MAX_RETRIES = 3;
    
    private final static String CHANGES_URL = "http://www.bbc.co.uk/iplayer/ion/ondemand/change/from_datetime/%S/format/json";
    
    private final AdapterLog log;
    private final ExecutorService updateProcessor;
    private final BbcIonOndemandChangeTaskBuilder updateTaskBuilder;
    private final SimpleHttpClient httpClient;
    
    private final BbcIonDeserializer<IonOndemandChanges> deserialiser = deserializerForClass(IonOndemandChanges.class);

    public BbcIonOndemandChangeUpdateBuilder(ContentResolver resolver, ContentWriter writer, AdapterLog log, SimpleHttpClient httpClient) {
        this.log = log;
        this.httpClient = httpClient;
        this.updateTaskBuilder = new BbcIonOndemandChangeTaskBuilder(resolver, writer, log);
        this.updateProcessor = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("Ion Ondemand Update Processor").build());
    }

    public BbcIonOndemandChangeUpdateBuilder(ContentResolver localFetcher, ContentWriter writer, AdapterLog log) {
        this(localFetcher, writer, log, new SimpleHttpClientBuilder().withUserAgent(ATLAS_USER_AGENT).withConnectionTimeout(60, SECONDS).withSocketTimeout(60, SECONDS).build());
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
            try {
                
                Set<Future<Void>> tasks = Sets.newHashSet();
                
                for (int i = 0; i < MAX_FORWARD_STEPS; i++) {

                    log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Fetching ondemand change tasks from %s. ",fromDateTime.toDateTime(DateTimeZones.UTC))));
                    String json = fetch(fromDateTime);
                    IonOndemandChanges changes = deserialiser.deserialise(json);
                    
                    for (IonOndemandChange change : changes.getBlocklist()) {
                        tasks.add(completer.submit(updateTaskBuilder.taskForChange(change)));
                    }
                    log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Added %s ondemand change tasks from %s. ",changes.getBlocklist().size(),fromDateTime.toDateTime(DateTimeZones.UTC))));

                    if (changes.getNextFromDatetime() == null || fromDateTime.equals(changes.getNextFromDatetime())) {
                        break;
                    }
                    fromDateTime = changes.getNextFromDatetime();
                }

                for (int t = 1; t <= tasks.size(); t++) {
                    completer.take();
                    if (t % 20 == 0) {
                        log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Completed %d ondemand change tasks", t)));
                    }
                }
                log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Completed %s ondemand change tasks",tasks.size())));
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e)
                        .withDescription("Unable to fetch ondemand changes for " + String.format(CHANGES_URL, fromDateTime.toString())));
            }
            
            return fromDateTime;
        }
        
        private String fetch(DateTime limitedLastRun) throws HttpException {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    return httpClient.getContentsOf(String.format(CHANGES_URL, limitedLastRun.toString()));
                } catch (HttpException e) {
                    
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
