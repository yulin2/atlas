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
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawBrandAdapter implements SiteSpecificAdapter<Brand> {
    
    private final Pattern seesawContentPagePattern = Pattern.compile("http://www.seesaw.com/(.*)/b-[0-9]+-(.*)");
    private final Pattern seriesLinkPattern = Pattern.compile("\\?/player.episodelist:.*/([0-9]+)"); //?/player.episodelist:updateepisodesevent/28458
    private SimpleHttpClient httpClient;
    private SiteSpecificAdapter<Series> seriesAdapter;
    private static final Log LOG = LogFactory.getLog(SeesawBrandAdapter.class);
    
    public SeesawBrandAdapter() {
        this(HttpClients.screenScrapingClient());
    }
    
    public SeesawBrandAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        this.seriesAdapter = new SeesawSeriesAdapter(httpClient);
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
                String brandName = matcher.group(2);
                
                Brand brand = new Brand(uri, SeesawHelper.getCurieFromTitle(brandName), Publisher.SEESAW);
                brand.addAlias(SeesawHelper.getCanonicalUriFromTitle(brandName));
                
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
                        Playlist series = seriesAdapter.fetch(properSeriesUri);
                        addSeriesToBrand(series, brand);
                    }
                }
                else {
                    Playlist series = seriesAdapter.fetch(uri);
                    addSeriesToBrand(series, brand);
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
    
    private void addSeriesToBrand(Playlist playlist, Brand brand) {
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
