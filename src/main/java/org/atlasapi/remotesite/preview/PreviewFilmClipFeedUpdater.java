package org.atlasapi.remotesite.preview;

import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.pa.film.PaFilmFeedUpdater;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class PreviewFilmClipFeedUpdater extends ScheduledTask {
    
    private final SimpleHttpClient client = HttpClients.webserviceClient();
    private final PreviewFilmProcessor processor;
    private final AdapterLog log;
    private final String feedUri;
    
    public PreviewFilmClipFeedUpdater(String feedUri, ContentWriter contentWriter, AdapterLog log) {
        this.feedUri = feedUri;
        this.log = log;
        this.processor = new PreviewFilmProcessor(contentWriter);
    }

    @Override
    protected void runTask() {
        try {
            String feedContents = client.getContentsOf(feedUri);
            reportStatus("Feed contents received");
            Builder builder = new Builder(new FilmProcessingNodeFactory());
            builder.build(new StringReader(feedContents));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withUri(feedUri).withDescription("Exception while fetching film feed"));
            throw new RuntimeException(e);
        } 
    }
    
    private class FilmProcessingNodeFactory extends NodeFactory {
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("movie") && shouldContinue()) {
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
