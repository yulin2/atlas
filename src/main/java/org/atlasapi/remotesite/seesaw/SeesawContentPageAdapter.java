package org.atlasapi.remotesite.seesaw;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawContentPageAdapter implements SiteSpecificAdapter<Brand> {
    
    private final Pattern seesawContentPagePattern = Pattern.compile("http://www.seesaw.com/(.*)/([bsp])-[0-9]+-(.*)");
    private final Pattern seriesLinkPattern = Pattern.compile("\\?/player.episodelist:.*/([0-9]+)"); //?/player.episodelist:updateepisodesevent/28458
    private final String BRAND = "b";
    private final String SERIES = "s";
    private final String PROGRAM = "p";
    private SimpleHttpClient httpClient;
    private SiteSpecificAdapter<Playlist> playlistAdapter;
    private static final Log LOG = LogFactory.getLog(SeesawContentPageAdapter.class);
    
    public SeesawContentPageAdapter() {
        this(HttpClients.screenScrapingClient());
    }
    
    public SeesawContentPageAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        this.playlistAdapter = new SeesawPlaylistAdapter(httpClient);
    }
    
    @Override
    public Brand fetch(String uri) {
        String content;
        try {
            content = httpClient.getContentsOf(uri);
        } catch (HttpException e) {
            LOG.warn("Error retrieving seesaw brands: " + uri + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
            return null;
        }

        if (content != null) {
            Matcher matcher = seesawContentPagePattern.matcher(uri);
            if (matcher.matches()) {
                String pageSection = matcher.group(1);
                String pageType = matcher.group(2);
                String brandName = matcher.group(3);
                
                /* TODO: 
                 * if brand page, check we don't already have this brand
                 */
                Brand brand = new Brand(SeesawHelper.getCanonicalUriFromTitle(brandName), SeesawHelper.getCurieFromTitle(brandName), Publisher.SEESAW);
                if (pageType.equalsIgnoreCase(BRAND)) {
                    brand.setCanonicalUri(uri);
                    brand.addAlias(SeesawHelper.getCanonicalUriFromTitle(brandName));
                }
                
                HtmlNavigator navigator = new HtmlNavigator(content);
                Element title = navigator.firstElementOrNull("//*[@id='title']");
                if (title != null) {
                    brand.setTitle(title.getText());
                }
                Element seriesList = navigator.firstElementOrNull("//*[@class='seriesList']");
                if (seriesList != null) {
                    List<String> seriesUris = SeesawHelper.getAllLinkUris(seriesList);
                    for (String seriesUri : seriesUris) {
                        String properSeriesUri = getSeriesUriFromJavascriptLink(seriesUri, pageSection, brandName);
                        Playlist playlist = playlistAdapter.fetch(properSeriesUri);
                        addPlaylistToBrand(playlist, brand);
                    }
                }
                else {
                    Playlist playlist = playlistAdapter.fetch(uri);
                    addPlaylistToBrand(playlist, brand);
                }
                
                return brand;
            }
        }
        
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        Matcher matcher = seesawContentPagePattern.matcher(uri);
        return matcher.matches();
    }
    
    private void addPlaylistToBrand(Playlist playlist, Brand brand) {
        if (playlist != null) {
            brand.getGenres().addAll(playlist.getGenres());
            
            if (brand.getDescription() == null) {
                brand.setDescription(playlist.getDescription());
            }
            
            for (Item item : playlist.getItems()) {
                brand.addItem(item);
            }
        }
    }

    private String getSeriesUriFromJavascriptLink(String jsLink, String pageSection, String brandName) {
        Matcher matcher = seriesLinkPattern.matcher(jsLink);
        if (matcher.matches()) {
            String seriesNumber = matcher.group(1);
            return "http://www.seesaw.com/" + pageSection + "/s-" + seriesNumber + "-" + brandName;
        }
        return null;
    }
}
