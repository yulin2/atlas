package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Element;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawPlaylistAdapter implements SiteSpecificAdapter<Playlist> {
    private static final String URL = "http://www.seesaw.com/TV/";
    static final Log LOG = LogFactory.getLog(SeesawAtoZBrandsAdapter.class);
    private final SimpleHttpClient httpClient;
    private final SiteSpecificAdapter<Episode> itemAdapter;
    
    public SeesawPlaylistAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        itemAdapter = new SeesawItemAdapter(httpClient);
    }
    
    @Override
    public Playlist fetch(String uri) {
        try {
            LOG.info("Retrieving Seesaw playlist");
            System.out.println("Attempting to load playlist " + uri);

            String content;
            try {
                content = httpClient.getContentsOf(uri);
            } catch (HttpException e) {
                LOG.error("error getting seesaw playlist contents", e);
                return null;
            }

            if (content != null) {
                HtmlNavigator navigator = new HtmlNavigator(content);
                Playlist playlist = new Playlist();
                
                List<Element> targetLinkElements = navigator.allElementsMatching("//a[contains(@class,'targetLink')]");
                
                if (targetLinkElements.isEmpty()) {
                    addEpisode(uri, playlist);
                }
                else {
                    for (Element targetLinkElement : targetLinkElements) {
                        addEpisode(targetLinkElement.getAttributeValue("href"), playlist);
                    }
                }
                
                return playlist;
            } else {
                LOG.error("Unable to retrieve seesaw playlist: " + uri);
            }
        } catch (JaxenException e) {
            LOG.warn("Error retrieving all hulu brands: " + uri + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
            throw new FetchException("Unable to retrieve all hulu brands", e);
        }
        
        return null;
    }

    private void addEpisode(String uri, Playlist playlist) {
        Episode episode = itemAdapter.fetch(uri);
        if (episode != null) {
            playlist.getGenres().addAll(episode.getGenres());
            
            Series series = episode.getSeriesSummary();
            if (series != null) {
                playlist.setDescription(series.getDescription());
            }
            
            playlist.addItem(episode);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return uri.startsWith(URL);
    }

}
