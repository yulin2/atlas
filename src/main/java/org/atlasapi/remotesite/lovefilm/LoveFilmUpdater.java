package org.atlasapi.remotesite.lovefilm;

import com.google.common.base.Throwables;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.channel4.RequestLimitingSimpleHttpClient;

/**
 */
class LoveFilmUpdater extends ScheduledTask {

    private static final String BASE_API_URL = "http://openapi.lovefilm.com/catalog";
    //
    private final Timestamper timestamper = new SystemClock();
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    private final AdapterLog log;
    private SimpleHttpClient client;

    public LoveFilmUpdater(ContentResolver contentResolver, ContentWriter contentWriter, AdapterLog log, String apiKey, String apiSecret) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
        this.log = log;
        this.client = new RequestLimitingSimpleHttpClient(HttpClients.oauthClient(apiKey, apiSecret), 2);
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("LoveFilm update started from " + BASE_API_URL).withSource(getClass()));

            client.get(new SimpleHttpRequest<Void>(BASE_API_URL + "/film?items_per_page=10", new LoveFilmFilmHttpTransformer(client, log, contentResolver, contentWriter)));

            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("LoveFilm update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing LoveFilm catalog."));
            Throwables.propagate(e);
        }
    }
}
