package org.atlasapi.remotesite.space;

import com.metabroadcast.common.http.IdentityHttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import java.io.InputStream;
import java.util.Iterator;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 */
public class TheSpaceItemsProcessor {

    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public TheSpaceItemsProcessor(SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.client = client;
        this.log = log;
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    public void process(InputStream content) throws Exception {
        TheSpaceItemProcessor processor = new TheSpaceItemProcessor(client, log, contentResolver, contentWriter);
        //
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(content);
        Iterator<JsonNode> results = root.get("results").getElements();
        while (results.hasNext()) {
            JsonNode item = results.next();
            String pid = item.get("pid").asText();
            try {
                InputStream stream = client.get(new SimpleHttpRequest<InputStream>(TheSpaceUpdater.BASE_API_URL + "/items/" + pid + ".json", new IdentityHttpResponseTransformer()));
                processor.process(content);
            } catch (Exception ex) {
                log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withCause(ex));
            }
        }
    }
}
