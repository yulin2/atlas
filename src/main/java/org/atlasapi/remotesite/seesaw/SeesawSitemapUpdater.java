package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.SavingFetcher;

import com.google.inject.internal.Lists;

public class SeesawSitemapUpdater implements Runnable {
    
    public static final String SITEMAP_INDEX = "http://www.seesaw.com/googlesitemaps/Sitemapindex";
    
    private final RemoteSiteClient<List<String>> sitemapIndexClient = new SeesawSitemapIndexClient();
    private final RemoteSiteClient<List<String>> sitemapClient = new SeesawSitemapClient();

    private final SavingFetcher savingFetcher;

    private final AdapterLog log;
    
    public SeesawSitemapUpdater(SavingFetcher savingFetcher, AdapterLog log) {
        this.savingFetcher = savingFetcher;
        this.log = log;
    }

    @Override
    public void run() {
        List<String> sitemaps = Lists.newArrayList();
        try {
            sitemaps = sitemapIndexClient.get(SITEMAP_INDEX);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(SITEMAP_INDEX).withSource(SeesawSitemapUpdater.class));
        }
        
        for (String sitemap: sitemaps) {
            try {
                List<String> urls = sitemapClient.get(sitemap);
                
                for (String url: urls) {
                    try {
                        savingFetcher.fetch(url);
                    } catch (Exception e) {
                        log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(url).withSource(SeesawSitemapUpdater.class));
                    }
                }
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(sitemap).withSource(SeesawSitemapUpdater.class));
            }
        }
    }
}
