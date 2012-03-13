package org.atlasapi.remotesite.space;

import com.google.common.base.Throwables;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import java.io.InputStream;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

/**
 */
public class TheSpaceHttpTransformer implements HttpResponseTransformer<Void> {

    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public TheSpaceHttpTransformer(SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.client = client;
        this.log = log;
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    @Override
    public Void transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
        try {
            new TheSpaceItemsProcessor(client, log, contentResolver, contentWriter).process(body);
        } catch (Exception ex) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withCause(ex));
            Throwables.propagate(ex);
        }
        return null;
    }
}
