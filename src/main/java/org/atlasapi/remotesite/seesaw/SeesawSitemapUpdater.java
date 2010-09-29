package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.SavingFetcher;
import org.atlasapi.remotesite.NoMatchingAdapterException;

import com.google.inject.internal.Lists;

public class SeesawSitemapUpdater implements Runnable {
    public static final String SITEMAP_INDEX = "http://www.seesaw.com/googlesitemaps/Sitemapindex";
    
    private final RemoteSiteClient<List<String>> sitemapIndexClient = new SeesawSitemapIndexClient();
    private final RemoteSiteClient<List<String>> sitemapClient = new SeesawSitemapClient();
    private static final Log log = LogFactory.getLog(SeesawSitemapUpdater.class);

    private final SavingFetcher savingFetcher;
    
    public SeesawSitemapUpdater(SavingFetcher savingFetcher) {
        this.savingFetcher = savingFetcher;
    }

    @Override
    public void run() {
        List<String> sitemaps = Lists.newArrayList();
        try {
            sitemaps = sitemapIndexClient.get(SITEMAP_INDEX);
        } catch (Exception e) {
            log.warn("Unable to retrieve sitemaps", e);
        }
        
        for (String sitemap: sitemaps) {
            try {
                List<String> urls = sitemapClient.get(sitemap);
                
                for (String url: urls) {
                    try {
                        savingFetcher.fetch(url);
                    } catch (NoMatchingAdapterException nmae) {
                        if (log.isInfoEnabled()) {
                            log.info("No matching adapter for: "+url);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Unable to retrieve sitemap: "+sitemap, e);
            }
        }
    }
}
