package org.atlasapi.remotesite.pa.film;

import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;

import com.metabroadcast.common.http.SimpleHttpClient;

public class PaFilmFeedUpdater implements Runnable {
    
    private final SimpleHttpClient client = HttpClients.webserviceClient();
    private final String feedUrl;
    private final AdapterLog log;
    private final PaFilmProcessor processor;
    
    public PaFilmFeedUpdater(String feedUrl, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.feedUrl = feedUrl;
        this.log = log;
        this.processor = new PaFilmProcessor(contentResolver, contentWriter, log);
    }

    @Override
    public void run() {
        String lastUpdated = "14/04/2011";
        
        String requestUri = feedUrl + "?lastUpdated=" + lastUpdated;
        try {
            Builder builder = new Builder(new FilmProcessingNodeFactory());
            builder.build(new StringReader(client.getContentsOf(requestUri)));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withUri(requestUri).withDescription("Exception while fetching film feed"));
        } 
    }

    private class FilmProcessingNodeFactory extends NodeFactory {
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("film")) {
                try {
                    processor.process(element);
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(PaFilmFeedUpdater.class).withCause(e).withDescription("Exception when processing film"));
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
    }
}
