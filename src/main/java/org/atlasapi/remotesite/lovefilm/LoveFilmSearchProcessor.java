package org.atlasapi.remotesite.lovefilm;

import com.metabroadcast.common.http.SimpleHttpClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;

import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

/**
 */
public class LoveFilmSearchProcessor {

    public String process(Document content, SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) throws Exception {
        Builder parser = new Builder();
        Nodes searches = content.getRootElement().query("/search");
        String result = null;
        if (searches.size() == 1) {
            Node search = searches.get(0);

            Nodes next = search.query("link[@rel='next']/@href");
            if (next.size() == 1) {
                result = next.get(0).getValue();
            }

            LoveFilmFilmProcessor processor = new LoveFilmFilmProcessor();
            Nodes titles = search.query("catalog_title");
            for (int i = 0; i < titles.size(); i++) {
                try {
                    Node title = titles.get(i);
                    processor.process(parser.build(new ByteArrayInputStream(title.toXML().getBytes(Charset.forName("UTF-8")))), client, log, contentResolver, contentWriter);
                } catch (Exception ex) {
                    log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withCause(ex));
                }
            }
        }
        return result;
    }
}
