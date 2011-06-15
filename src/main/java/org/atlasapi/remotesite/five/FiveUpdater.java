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
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;

public class FiveUpdater extends ScheduledTask {
    
    private static final String BASE_API_URL = "https://pdb.five.tv/internal";
    
    private final AdapterLog log;
    private final FiveBrandProcessor processor;
    private final Timestamper timestamper = new SystemClock();
    private final RemoteSiteClient<HttpResponse> httpClient;

    public FiveUpdater(ContentWriter contentWriter, AdapterLog log) {
        this.log = log;
        this.httpClient = new RequestLimitingRemoteSiteClient<HttpResponse>(new HttpRemoteSiteClient(HttpClients.webserviceClient()), 4);
        this.processor = new FiveBrandProcessor(contentWriter, log, BASE_API_URL, httpClient);
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update started from " + BASE_API_URL).withSource(getClass()));
            
            reportStatus("Fetching " + BASE_API_URL + "/shows");
            String show = httpClient.get(BASE_API_URL + "/shows").body();

            Builder parser = new Builder(new ShowProcessingNodeFactory());
            parser.build(new StringReader(show));
            
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
                int processed = 0, failed = 0;
                try {
                    processor.processShow(element);
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(FiveUpdater.class).withCause(e).withDescription("Exception when processing show"));
                    failed++;
                } finally {
                    reportStatus(String.format("%s processed. %s failed", ++processed, failed));
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
    }
}
