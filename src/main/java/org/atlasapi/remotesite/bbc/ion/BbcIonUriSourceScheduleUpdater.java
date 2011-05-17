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
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonUriSourceScheduleUpdater extends ScheduledTask {

    protected static final int THREADS = 5;
    private final Iterable<String> uriSource;
    private final ContentResolver localFetcher;
    private final AdapterLog log;
    
    private final ContentWriter writer;
    private final BbcIonDeserializer<IonSchedule> deserialiser;
    private BbcItemFetcherClient fetcherClient;
    private SimpleHttpClient httpClient;
    private final ItemsPeopleWriter itemsPeopleWriter;

    public BbcIonUriSourceScheduleUpdater(Iterable<String> uriSource, ContentResolver localFetcher, ContentWriter writer, BbcIonDeserializer<IonSchedule> deserialiser, ItemsPeopleWriter itemsPeopleWriter, AdapterLog log) {
        this.uriSource = uriSource;
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.deserialiser = deserialiser;
        this.itemsPeopleWriter = itemsPeopleWriter;
        this.log = log;
    }

    public BbcIonUriSourceScheduleUpdater withItemFetchClient(BbcItemFetcherClient fetcherClient) {
        this.fetcherClient = fetcherClient;
        return this;
    }
    
    public BbcIonUriSourceScheduleUpdater withHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }
    
    @Override
    public void runTask() {
        DateTime start = new DateTime(DateTimeZones.UTC);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update initiated"));

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        
        for (String uri : uriSource) {
            if (!shouldContinue()) {
                break;
            }
            reportStatus(uri);
            BbcIonScheduleUpdateTask updateTask = new BbcIonScheduleUpdateTask(uri, this.httpClient != null ? this.httpClient : HttpClients.webserviceClient(), localFetcher, writer, deserialiser, itemsPeopleWriter, log, fetcherClient);
            executor.submit(updateTask);
        }
        
        executor.shutdown();
        boolean completion = false;
        try {
            completion = executor.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("BBC Ion Schedule Update interrupted waiting for completion").withCause(e));
        }
        
        String runTime = new Period(start, new DateTime(DateTimeZones.UTC)).toString(PeriodFormat.getDefault());
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update finished in " + runTime + (completion ? "" : " (timed-out)")));
    }
}
