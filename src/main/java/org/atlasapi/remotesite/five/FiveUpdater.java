package org.atlasapi.remotesite.five;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.channel4.RequestLimitingRemoteSiteClient;

import com.google.common.base.Throwables;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;

public class FiveUpdater extends ScheduledTask {
    
    private static final String BASE_API_URL = "https://pdb.five.tv/internal";
    
    private final AdapterLog log;
    private final FiveBrandProcessor processor;
    private final Timestamper timestamper = new SystemClock();
    private final int socketTimeout;

    private final Builder parser = new Builder();
    private SimpleHttpClient streamHttpClient;

    public FiveUpdater(ContentWriter contentWriter, ChannelResolver channelResolver, AdapterLog log, int socketTimeout) {
        this.log = log;
        this.socketTimeout = socketTimeout;
        this.streamHttpClient = buildFetcher(log);
        this.processor = new FiveBrandProcessor(contentWriter, log, BASE_API_URL, 
            new RequestLimitingRemoteSiteClient<HttpResponse>(new HttpRemoteSiteClient(buildFetcher(log)), 4), 
            channelMap(channelResolver)
        );
    }

    private Map<String, Channel>channelMap(ChannelResolver channelResolver) {
        return new FiveChannelMap(channelResolver); 
    }

    private SimpleHttpClient buildFetcher(final AdapterLog log) {
        return new SimpleHttpClientBuilder()
            .withUserAgent(HttpClients.ATLAS_USER_AGENT)
            .withSocketTimeout(socketTimeout, TimeUnit.SECONDS)
            .withTrustUnverifiedCerts()
            .withRetries(3)
            .build();
    }
    
    private final HttpResponseTransformer<Document> TRANSFORMER = new HttpResponseTransformer<Document>() {
        @Override
        public Document transform(HttpResponsePrologue response, InputStream in) throws HttpException, IOException {
            try {
                return parser.build(in);
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing shows document"));
                Throwables.propagate(e);
            }
            return null;
        }
    };

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update started from " + BASE_API_URL).withSource(getClass()));
            
            process(streamHttpClient.get(new SimpleHttpRequest<Document>(BASE_API_URL + "/shows", TRANSFORMER)));
            
            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(Severity.INFO).withDescription("Five update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing shows document"));
            Throwables.propagate(e);
        }
    }
    
    private void process(Document document) {
        int processed = 0, failed = 0;
        
        Elements elements = document.getRootElement().getFirstChildElement("shows").getChildElements();
        for(int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
        
            if (element.getLocalName().equalsIgnoreCase("show")) {
                try {
                    processor.processShow(element);
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(FiveUpdater.class).withCause(e).withDescription("Exception when processing show"));
                    failed++;
                }
                reportStatus(String.format("%s processed. %s failed", ++processed, failed));
            }
        }
    }

}
