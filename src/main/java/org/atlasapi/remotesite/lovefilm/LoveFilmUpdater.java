package org.atlasapi.remotesite.lovefilm;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;
import java.io.IOException;
import java.io.InputStream;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.remotesite.HttpClients;

/**
 */
class LoveFilmUpdater extends ScheduledTask {

    private static final String BASE_API_URL = "http://openapi.lovefilm.com/catalog";
    private static final HttpResponseTransformer<Void> FILM_TRANSFORMER = new HttpResponseTransformer<Void>() {

        @Override
        public Void transform(HttpResponsePrologue response, InputStream in) throws HttpException, IOException {
            try {
                System.out.println(new String(ByteStreams.toByteArray(in)));
            } catch (Exception e) {
                Throwables.propagate(e);
            }
            return null;
        }
    };
    private static final HttpResponseTransformer<Void> TV_TRANSFORMER = new HttpResponseTransformer<Void>() {

        @Override
        public Void transform(HttpResponsePrologue response, InputStream in) throws HttpException, IOException {
            try {
                System.out.println(new String(ByteStreams.toByteArray(in)));
            } catch (Exception e) {
                Throwables.propagate(e);
            }
            return null;
        }
    };
    //
    private final Timestamper timestamper = new SystemClock();
    private final ContentWriter contentWriter;
    private final AdapterLog log;
    private SimpleHttpClient client;
    //private final FiveBrandProcessor processor;
    //private final Builder parser = new Builder(new FiveUpdater.ShowProcessingNodeFactory());

    public LoveFilmUpdater(ContentWriter contentWriter, AdapterLog log, String apiKey, String apiSecret) {
        this.contentWriter = contentWriter;
        this.log = log;
        this.client = HttpClients.oauthClient(apiKey, apiSecret);
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("LoveFilm update started from " + BASE_API_URL).withSource(getClass()));

            client.get(new SimpleHttpRequest<Void>(BASE_API_URL + "/film?items_per_page=1", FILM_TRANSFORMER));
            //client.get(new SimpleHttpRequest<Void>(BASE_API_URL + "/tv?term=dexter", TV_TRANSFORMER));

            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("LoveFilm update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing LoveFilm catalog."));
            Throwables.propagate(e);
        }
    }
}
