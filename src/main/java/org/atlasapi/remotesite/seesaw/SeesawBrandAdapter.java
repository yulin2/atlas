package org.atlasapi.remotesite.seesaw;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawBrandAdapter implements SiteSpecificAdapter<Brand> {
    
    private final Pattern seesawContentPagePattern = Pattern.compile("http://www.seesaw.com/(.*)/b-[0-9]+-(.*)");
    private final Pattern seriesLinkPattern = Pattern.compile("\\?/player.episodelist:.*/([0-9]+)"); //?/player.episodelist:updateepisodesevent/28458
    private SimpleHttpClient httpClient;
    private SiteSpecificAdapter<Series> seriesAdapter;
    
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
            throw new FetchException("Error retrieving seesaw brands: " + uri, e);
        }

        if (content != null) {
            Matcher matcher = seesawContentPagePattern.matcher(uri);
            if (matcher.matches()) {
                String pageSection = matcher.group(1);
                String brandName = matcher.group(2);
                
                Brand brand = new Brand(uri, SeesawHelper.getCurieFromTitle(brandName), Publisher.SEESAW);
                
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
                        Series series = seriesAdapter.fetch(properSeriesUri);
                        addSeriesToBrand(series, brand);
                    }
                }
                else {
                    Series series = seriesAdapter.fetch(uri);
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
    
    private void addSeriesToBrand(Series series, Brand brand) {
        if (series != null) {
            brand.setGenres(Iterables.concat(brand.getGenres(), series.getGenres()));
            
            if (brand.getDescription() == null) {
                brand.setDescription(series.getDescription());
            }
            if (brand.getImage() == null && series.getImage() != null) {
                brand.setImage(series.getImage());
            }
            
            List<Episode> episodes = Lists.newArrayListWithCapacity(series.getContents().size());
            for (Content item : series.getContents()) {
                Episode episode = (Episode) item;
                if (series.getCanonicalUri().equals(brand.getCanonicalUri())) {
                    episode.setSeries(null);
                }
                episodes.add(episode);
            }
            brand.addContents(episodes);
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
