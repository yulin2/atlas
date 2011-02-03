package org.atlasapi.remotesite.bbc.ion;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.atlasapi.remotesite.HttpClients.ATLAS_USER_AGENT;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.joda.time.DateTime;

import com.google.common.collect.Ordering;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonOndemandChangeUpdater implements Runnable {

    private final static String CHANGES_URL = "http://www.bbc.co.uk/iplayer/ion/ondemand/change/from_datetime/%S/format/json";
    public final static String SLASH_PROGRAMMES_BASE = "http://www.bbc.co.uk/programmes/";

    private final SimpleHttpClient httpClient = new SimpleHttpClientBuilder().withUserAgent(ATLAS_USER_AGENT)
        .withConnectionTimeout(60, SECONDS).withSocketTimeout(60, SECONDS).withRetries(3).build();
    private final ContentResolver localFetcher;
    private final ContentWriter writer;
    private final AdapterLog log;
    private DateTime lastRun = new DateTime(DateTimeZones.UTC).minusHours(2);
    private boolean isRunning = false;
    private final BbcIonDeserializer<IonOndemandChanges> deserialiser;
    private final BbcIonOndemandItemUpdater itemUpdater = new BbcIonOndemandItemUpdater();

    public BbcIonOndemandChangeUpdater(ContentResolver localFetcher, ContentWriter writer, BbcIonDeserializer<IonOndemandChanges> deserialiser, AdapterLog log) {
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.deserialiser = deserialiser;
        this.log = log;
    }

    @Override
    public void run() {
        if (!isRunning) {
            isRunning = true;
            try {
                DateTime limitedLastRun = Ordering.natural().max(new DateTime(DateTimeZones.UTC).minusHours(2), lastRun);
                String json = httpClient.getContentsOf(String.format(CHANGES_URL, limitedLastRun.toString()));
                IonOndemandChanges changes = deserialiser.deserialise(json);

                for (IonOndemandChange change : changes.getBlocklist()) {
                    String uri = SLASH_PROGRAMMES_BASE + change.getEpisodeId();
                    try {

                        Item item = (Item) localFetcher.findByCanonicalUri(uri);
                        if (item != null) {
                            itemUpdater.updateItemDetails(item, change);
                            writer.createOrUpdate(item);
                        }
                    } catch (Exception e) {
                        log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Unable to process ondemand changes for item " + uri));
                    }
                }
                limitedLastRun = new DateTime(DateTimeZones.UTC);
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription(
                        "Unable to process ondemand changes for " + String.format(CHANGES_URL, lastRun.toString())));
            } finally {
                isRunning = false;
            }
        }
    }

}
