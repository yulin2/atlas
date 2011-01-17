package org.atlasapi.remotesite.seesaw;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

public class SeesawRssUpdater implements Runnable {
    
    private final DefinitiveContentWriter contentWriter;
    private final AdapterLog log;
    private final String feedUri;
    
    public SeesawRssUpdater(DefinitiveContentWriter contentWriter, AdapterLog log, String feedUri) {
        this.contentWriter = contentWriter;
        this.log = log;
        this.feedUri = feedUri;
    }
    
    @Override
    public void run() {
        try {
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Seesaw RSS update started"));
            
            SeesawContentProcessor contentProcessor = new SeesawContentProcessor(log);
            Builder parser = new Builder(new ItemProcessingNodeFactory(contentProcessor));
            parser.build(feedUri);
            
            contentProcessor.joinContent();
            
            for (Brand brand : contentProcessor.getAllBrands()) {
                contentWriter.createOrUpdateDefinitivePlaylist(brand);
            }
            
            for (Item item : contentProcessor.getAllItems()) {
                contentWriter.createOrUpdateDefinitiveItem(item);
            }
            
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Seesaw RSS update completed"));
        }
        catch (Exception ex) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(ex).withSource(SeesawRssUpdater.class));
        }
    }
    
    private class ItemProcessingNodeFactory extends NodeFactory {
        
        private final SeesawContentProcessor contentProcessor;

        public ItemProcessingNodeFactory(SeesawContentProcessor contentProcessor) {
            this.contentProcessor = contentProcessor;
        }
        
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("item")) {
                
                try {
                    contentProcessor.processItemElement(element);
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(SeesawRssUpdater.class));
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
    }
}
