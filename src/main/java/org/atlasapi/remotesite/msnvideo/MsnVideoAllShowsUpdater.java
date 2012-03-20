package org.atlasapi.remotesite.msnvideo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.atlasapi.media.content.Brand;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.xml.SimpleXmlNavigator;
import org.jdom.Element;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class MsnVideoAllShowsUpdater implements Runnable {
    
    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final SiteSpecificAdapter<Brand> brandAdapter;
    private final ContentWriter contentWriter;

    public MsnVideoAllShowsUpdater(SimpleHttpClient client, AdapterLog log, SiteSpecificAdapter<Brand> brandAdapter, ContentWriter contentWriter) {
        this.client = client;
        this.log = log;
        this.brandAdapter = brandAdapter;
        this.contentWriter = contentWriter;
    }

    @Override
    public void run() {
        try {
            for (int page = 1; page < 100; page++) {
                SimpleXmlNavigator navigator = getNavigatorForPage(page++);
                List<Element> items = navigator.allElementsMatching("//a[@class='item']");
                
                if (items.isEmpty()) {
                    break;
                }
                
                for (Element item : items) {
                    String link = item.getAttributeValue("href");
                    if (brandAdapter.canFetch(link)) {
                        Brand brand = brandAdapter.fetch(link);
                        if (brand != null) {
                            contentWriter.createOrUpdate(brand);
                        }
                    }
                    else {
                        log.record(new AdapterLogEntry(Severity.INFO).withDescription("Brand adapter could not fetch " + link).withSource(MsnVideoAllShowsUpdater.class));
                    }
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(MsnVideoAllShowsUpdater.class));
        }
    }
    
    private SimpleXmlNavigator getNavigatorForPage(int page) throws UnsupportedEncodingException, HttpException {
        String baseUrl = "http://video.uk.msn.com/browse/tv-shows/genres?";
        String tagQuery = "<tagQuery><tags><tag namespace=\"videotype\">tv</tag></tags><source>Msn</source><dataCatalog>Video</dataCatalog></tagQuery>";
        String queryUrl = "rt=ajax&tagquery=" + URLEncoder.encode(tagQuery, "UTF-8") + "&currentpage=" + page + "&currentletter=&id=ux1_4_2_4";
        
        String content = client.getContentsOf(baseUrl + queryUrl);
        
        return new SimpleXmlNavigator(content);
    }
}
