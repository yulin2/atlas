package org.atlasapi.remotesite.space;

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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 */
class TheSpaceUpdater extends ScheduledTask {

    public static final String BASE_API_URL = "https://web.dev.thespace.org";
    //
    private final Timestamper timestamper = new SystemClock();
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    private final AdapterLog log;
    private SimpleHttpClient client;

    public TheSpaceUpdater(ContentResolver contentResolver, ContentWriter contentWriter, AdapterLog log, String keystore, String password) throws Exception {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
        this.log = log;
        this.client = new RequestLimitingSimpleHttpClient(HttpClients.httpsClient(keystore, password), 10);
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("LoveFilm update started from " + BASE_API_URL).withSource(getClass()));

            TheSpaceItemsProcessor processor = new TheSpaceItemsProcessor(client, log, contentResolver, contentWriter);
            JsonNode items = client.get(new SimpleHttpRequest<JsonNode>(BASE_API_URL + "/items.json", new JSonNodeHttpResponseTransformer(new ObjectMapper())));
            processor.process(items);

            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("LoveFilm update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing LoveFilm catalog."));
            Throwables.propagate(e);
        }
    }
}
