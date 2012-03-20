package org.atlasapi.remotesite.lovefilm;

import com.google.common.base.Throwables;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import java.io.InputStream;
import nu.xom.Builder;
import nu.xom.Document;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

/**
 */
public class LoveFilmFilmHttpTransformer implements HttpResponseTransformer<Void> {

    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public LoveFilmFilmHttpTransformer(SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.client = client;
        this.log = log;
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    @Override
    public Void transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
        try {
            LoveFilmSearchProcessor processor = new LoveFilmSearchProcessor();
            Builder parser = new Builder();
            Document current = parser.build(body);
            String next = null;
            do {
                try {
                    next = processor.process(current, client, log, contentResolver, contentWriter);
                    if (next != null) {
                        current = client.get(new SimpleHttpRequest<Document>(next, new XmlHttpResponseTransformer()));
                    } else {
                        current = null;
                    }
                } catch (Exception ex) {
                    log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withCause(ex));
                }
            } while (current != null);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return null;
    }
}
