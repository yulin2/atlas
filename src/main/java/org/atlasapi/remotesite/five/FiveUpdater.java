package org.atlasapi.remotesite.five;

import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.channel4.RequestLimitingRemoteSiteClient;

import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;

public class FiveUpdater implements Runnable {
    
    private final AdapterLog log;
    private final String baseApiUrl;
    private final FiveBrandProcessor processor;
    private final Timestamper timestamper = new SystemClock();
    private final RemoteSiteClient<HttpResponse> httpClient;

    public FiveUpdater(ContentWriter contentWriter, AdapterLog log, String baseApiUrl) {
        this.log = log;
        this.baseApiUrl = baseApiUrl;
        this.httpClient = new RequestLimitingRemoteSiteClient<HttpResponse>(new HttpRemoteSiteClient(HttpClients.webserviceClient()), 4);
        this.processor = new FiveBrandProcessor(contentWriter, log, baseApiUrl, httpClient);
    }

    @Override
    public void run() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update started").withSource(getClass()));
            
            Builder parser = new Builder(new ShowProcessingNodeFactory());
            parser.build(new StringReader(httpClient.get(baseApiUrl + "/shows").body()));
            
            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
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
