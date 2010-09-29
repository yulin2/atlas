package org.atlasapi.remotesite.seesaw;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawItemAdapter implements SiteSpecificAdapter<Episode> {
    private final Pattern seesawContentPagePattern = Pattern.compile("http://www.seesaw.com/(.*)/p-[0-9]+-(.*)");
    static final Log LOG = LogFactory.getLog(SeesawItemAdapter.class);
    private final SimpleHttpClient httpClient;
    private final ContentExtractor<HtmlNavigator, Episode> contentExtractor;
    
    public SeesawItemAdapter() {
        this(HttpClients.screenScrapingClient());
    }
    
    public SeesawItemAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        contentExtractor = new SeesawItemContentExtractor();
    }
    
    @Override
    public Episode fetch(String uri) { 
        if (LOG.isInfoEnabled()) {
            LOG.info("Retrieving SeeSaw item: "+uri);
        }
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
            Episode episode = contentExtractor.extract(navigator);
            
            setUris(uri, episode);
            
            return episode;
        } else {
            LOG.error("Unable to retrieve seesaw playlist: " + uri);
        }
        
        return null;
    }
    
    private void setUris(String uri, Episode episode) {
        episode.setCanonicalUri(uri);
        episode.setCurie(SeesawHelper.getCurieFromLink(uri));
        
        for (Version version : episode.getVersions()) {
            for (Encoding encoding : version.getManifestedAs()) {
                for (Location location : encoding.getAvailableAt()) {
                    location.setUri(uri);
                }
            }
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return seesawContentPagePattern.matcher(uri).matches();
    }
}
