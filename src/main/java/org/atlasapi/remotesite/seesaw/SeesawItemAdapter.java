package org.atlasapi.remotesite.seesaw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawItemAdapter implements SiteSpecificAdapter<Episode> {
    private static final String URL = "http://www.seesaw.com/TV/";
    static final Log LOG = LogFactory.getLog(SeesawAllBrandsAdapter.class);
    private final SimpleHttpClient httpClient;
    private final ContentExtractor<HtmlNavigator, Episode> contentExtractor;
    
    public SeesawItemAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        contentExtractor = new SeesawItemContentExtractor();
    }
    
    @Override
    public Episode fetch(String uri) { 
        LOG.info("Retrieving all Seesaw brands");

        String content = null;

        try {
            content = httpClient.getContentsOf(uri);
        } catch (HttpException e) {
            String warnString = "Error retrieving seesaw item: " + uri +" with message: " + e.getMessage();
            if (e.getCause() != null) {
                warnString += " with cause: " + e.getCause().getMessage();
            }
            LOG.warn(warnString);
            return null;
        }

        if (content != null) {
            HtmlNavigator navigator = new HtmlNavigator(content);
            return contentExtractor.extract(navigator);
        } else {
            LOG.error("Unable to retrieve seesaw playlist: " + uri);
        }
        
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        // TODO Auto-generated method stub
        return false;
    }
}
