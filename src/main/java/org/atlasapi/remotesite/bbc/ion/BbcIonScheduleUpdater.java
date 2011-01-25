package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;

import com.metabroadcast.common.http.SimpleHttpClient;


public class BbcIonScheduleUpdater implements Runnable {

    private final Iterable<String> uriSource;
    private final ContentResolver localFetcher;
    private final AdapterLog log;
    
    private final SimpleHttpClient httpClient;
    private final DefinitiveContentWriter writer;

    public BbcIonScheduleUpdater(Iterable<String> uriSource, ContentResolver localFetcher, DefinitiveContentWriter writer, AdapterLog log) {
        this.uriSource = uriSource;
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.log = log;
        httpClient = HttpClients.webserviceClient();
    }
    
    @Override
    public void run() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update initiated"));

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (String uri : uriSource) {
            executor.submit(new BbcIonScheduleUpdateTask(uri));
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

    private class BbcIonScheduleUpdateTask implements Runnable {

        private final String uri;

        public BbcIonScheduleUpdateTask(String uri) {
            this.uri = uri;
        }

        @Override
        public void run() {
            try {
                IonSchedule schedule = BbcIonScheduleDeserialiser.deserialise(httpClient.getContentsOf(uri));
                for (IonBroadcast broadcast : schedule.getBlocklist()) {
                    Item item = (Item) localFetcher.findByUri("http://www.bbc.co.uk/programmes/" + broadcast.getEpisodeId());
                    if(item != null) {
                        //updateItemDetails(item, broadcast.getEpisode());
                        if(!(item instanceof Episode)) {
                            //save and return
                        } else {
                            //update episode-only details
                        }
                    } else {
                        //item = createEpisodeFrom(broadcast);
                    }
                    //here item is and episode and not null;
                    Brand brand = (Brand) localFetcher.findByUri("http://www.bbc.co.uk/programmes/" + broadcast.getBrandId());
                    if(brand != null) {
                        //update brand details
                    } else {
                        //brand = createBrandFrom(brodcast);
                    }
                    //ensure item in brand,
                    //write brand
                }
            } catch(Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri).withSource(getClass()));
            }
        }

    }
    
}
