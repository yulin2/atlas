package org.atlasapi.remotesite.hbo;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.metabroadcast.common.http.SimpleHttpClient;

public class HboSiteMapUpdater implements Runnable {
    
    private static final String SITEMAP_URL = "http://www.hbo.com/sitemap.xml";
    private final SimpleHttpClient client;
    private final SiteSpecificAdapter<Brand> brandAdapter;
    private final ContentWriters contentWriter;
    private final AdapterLog log;
    
    public HboSiteMapUpdater(SimpleHttpClient client, SiteSpecificAdapter<Brand> brandAdapter, ContentWriters contentWriter, AdapterLog log) {
        this.client = client;
        this.brandAdapter = brandAdapter;
        this.contentWriter = contentWriter;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            String content = client.getContentsOf(SITEMAP_URL);
            
            if (content != null) {
                HtmlNavigator navigator = new HtmlNavigator(content);
                
                List<Element> linkElements = navigator.allElementsMatching("//url/loc");
                
                for (Element linkElement : linkElements) {
                    if (brandAdapter.canFetch(linkElement.getValue())) {
                        Brand brand = brandAdapter.fetch(linkElement.getValue());
                        if (brand != null) {
                            contentWriter.createOrUpdate(brand, true);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(HboSiteMapUpdater.class).withUri(SITEMAP_URL));
        }
    }
}
