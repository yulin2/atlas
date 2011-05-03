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
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.hp.hpl.jena.sparql.function.library.date;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.url.UrlEncoding;

public class PaFilmFeedUpdater extends ScheduledTask {
    
    private final static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
    private final static DateTime START_DATE = new DateTime(2011, DateTimeConstants.APRIL, 12, 0, 0, 0, 0);
    
    private final SimpleHttpClient client = HttpClients.webserviceClient();
    private final String feedUrl;
    private final AdapterLog log;
    private final PaFilmProcessor processor;
    private final boolean doCompleteUpdate;
    
    public PaFilmFeedUpdater(String feedUrl, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter, PaFilmProcessor processor) {
        this(feedUrl, log, contentResolver, contentWriter, processor, false);
    }
    
    private PaFilmFeedUpdater(String feedUrl, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter, PaFilmProcessor processor, boolean doCompleteUpdate) {
        this.feedUrl = feedUrl;
        this.log = log;
        this.processor = processor;
        this.doCompleteUpdate = doCompleteUpdate;
    }
    
    public static PaFilmFeedUpdater completeUpdater(String feedUrl, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter, PaFilmProcessor processor) {
        return new PaFilmFeedUpdater(feedUrl, log, contentResolver, contentWriter, processor, true);
    }
    
    @Override
    protected void runTask() {
        String requestUri = feedUrl;
        
        if (doCompleteUpdate) {
            requestUri += "/since?lastUpdated=" + UrlEncoding.encode(dateFormat.print(START_DATE));
        } else {
            requestUri += "/since?lastUpdated=" + UrlEncoding.encode(dateFormat.print(new DateTime(DateTimeZone.UTC).minusDays(3)));
        }
        
        try {
            String feedContents = client.getContentsOf(requestUri);
            reportStatus("Feed contents received");
            Builder builder = new Builder(new FilmProcessingNodeFactory());
            builder.build(new StringReader(feedContents));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withUri(requestUri).withDescription("Exception while fetching film feed"));
            throw new RuntimeException(e);
        } 
    }

    private class FilmProcessingNodeFactory extends NodeFactory {
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("film") && shouldContinue()) {
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
