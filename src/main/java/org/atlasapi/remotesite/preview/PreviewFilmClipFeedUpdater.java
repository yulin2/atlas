package org.atlasapi.remotesite.preview;

import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.rt.RtFilmFeedUpdater;

import com.google.common.base.Optional;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class PreviewFilmClipFeedUpdater extends ScheduledTask {
    
    private static final String LAST_UPDATE_KEY = "last_update";
    
    private final SimpleHttpClient client = new SimpleHttpClientBuilder()
        .withUserAgent(HttpClients.ATLAS_USER_AGENT)
        .withSocketTimeout(5, TimeUnit.MINUTES)
        .withConnectionTimeout(1, TimeUnit.MINUTES)
    .build();
    
    private final PreviewFilmProcessor processor;
    private final AdapterLog log;
    private final String feedUri;

    private final PreviewLastUpdatedStore lastUpdatedStore;
    
    public PreviewFilmClipFeedUpdater(String feedUri, ContentWriter contentWriter, AdapterLog log, PreviewLastUpdatedStore lastUpdatedStore) {
        this.feedUri = feedUri;
        this.log = log;
        this.lastUpdatedStore = lastUpdatedStore;
        this.processor = new PreviewFilmProcessor(contentWriter);
    }

    @Override
    protected void runTask() {
        try {
            Optional<String> lastUpdated = lastUpdatedStore.retrieve();
            String uri;
            if (lastUpdated.isPresent()) {
                uri = feedUri + "?" + LAST_UPDATE_KEY + "=" + lastUpdated.get();
            } else {
                uri = feedUri;
            }
            
            reportStatus("Requesting feed contents (" + uri + ")");
            String feedContents = client.getContentsOf(uri);
            reportStatus("Feed contents received");
            
            FilmProcessingNodeFactory filmProcessingNodeFactory = new FilmProcessingNodeFactory();
            Builder builder = new Builder(filmProcessingNodeFactory);
            builder.build(new StringReader(feedContents));
            reportStatus("Finished. Processed " + filmProcessingNodeFactory.getFilmCount() + " films");
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withUri(feedUri).withDescription("Exception while fetching film feed"));
            throw new RuntimeException(e);
        } 
    }
    
    private class FilmProcessingNodeFactory extends NodeFactory {
        int currentFilmNumber = 0;
        
        @Override
        public Nodes finishMakingElement(Element element) {
            
            if (element.getLocalName().equalsIgnoreCase("previewnetworks") && shouldContinue()) {
                String lastUpdated = element.getAttributeValue("last_upd_date");
                if (lastUpdated != null) {
                    lastUpdatedStore.store(lastUpdated);
                }
                
                return super.finishMakingElement(element);
            } else if (element.getLocalName().equalsIgnoreCase("movie") && shouldContinue()) {
                currentFilmNumber++;
                reportStatus("Processing film number " + currentFilmNumber);
                
                try {
                    processor.process(element);
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(RtFilmFeedUpdater.class).withCause(e).withDescription("Exception when processing film"));
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
        
        public int getFilmCount() {
            return currentFilmNumber;
        }
    }
}
