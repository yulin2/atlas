package org.atlasapi.remotesite.itunes;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.xml.SimpleXmlNavigator;
import org.jdom.Element;

import com.google.common.collect.Sets;
import com.metabroadcast.common.http.SimpleHttpClient;

public class ItunesRssUpdater implements Runnable {
    
    private final Iterable<String> feedUris;
    private final SimpleHttpClient client;
    private final SiteSpecificAdapter<Brand> brandAdapter;
    private final AdapterLog log;
    private final ContentWriter contentWriter;

    public ItunesRssUpdater(Iterable<String> feedUris, SimpleHttpClient client, ContentWriter contentWriter, SiteSpecificAdapter<Brand> brandAdapter, AdapterLog log) {
        this.feedUris = feedUris;
        this.client = client;
        this.contentWriter = contentWriter;
        this.brandAdapter = brandAdapter;
        this.log = log;
    }

    @Override
    public void run() {
        Set<String> alreadyProcessed = Sets.newHashSet();
        
        for (String feedUri : feedUris) {
            try {
                String content = client.getContentsOf(feedUri);
                
                if (content != null) {
                    SimpleXmlNavigator navigator = new SimpleXmlNavigator(content);
                    
                    List<Element> linkElements = navigator.allElementsMatching("//entry/artist");
                    
                    for (Element linkElement : linkElements) {
                        String brandUri = linkElement.getAttributeValue("href");
                        
                        if (!alreadyProcessed.contains(brandUri) && brandAdapter.canFetch(brandUri)) {
                            Brand brand = brandAdapter.fetch(brandUri);
                            if (brand != null) {
                                contentWriter.createOrUpdatePlaylist(brand, true);
                            }
                            alreadyProcessed.add(brandUri);
                        }
                    }
                }
            }
            catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesRssUpdater.class).withUri(feedUri));
            }
        }
    }
}
