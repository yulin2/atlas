package org.atlasapi.remotesite.five;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

public class FiveUpdater implements Runnable {
    
    private final AdapterLog log;
    private final String baseApiUrl;
    private final FiveBrandProcessor processor;

    public FiveUpdater(ContentWriter contentWriter, AdapterLog log, String baseApiUrl) {
        this.log = log;
        this.baseApiUrl = baseApiUrl;
        this.processor = new FiveBrandProcessor(contentWriter, log, baseApiUrl);
    }

    @Override
    public void run() {
        try {
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update started").withSource(getClass()));
            
            Builder parser = new Builder(new ShowProcessingNodeFactory());
            parser.build(baseApiUrl + "/shows");
            
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update completed").withSource(getClass()));
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing shows document"));
        }
    }
    
    private class ShowProcessingNodeFactory extends NodeFactory {
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("show")) {
                try {
                    processor.processShow(element);
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(FiveUpdater.class).withCause(e).withDescription("Exception when processing show"));
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
    }
}
