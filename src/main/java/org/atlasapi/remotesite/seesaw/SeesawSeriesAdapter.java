package org.atlasapi.remotesite.seesaw;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Element;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawSeriesAdapter implements SiteSpecificAdapter<Series> {
    private final Pattern seesawContentPagePattern = Pattern.compile("http://www.seesaw.com/(.*)/s-[0-9]+-(.*)");
    
    static final Log LOG = LogFactory.getLog(SeesawSeriesAdapter.class);
    private final SimpleHttpClient httpClient;
    private final SiteSpecificAdapter<Episode> itemAdapter;
    
    public SeesawSeriesAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        itemAdapter = new SeesawItemAdapter(httpClient);
    }
    
    @Override
    public Series fetch(String uri) {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Retrieving Seesaw playlist: "+uri);
            }

            String content;
            try {
                content = httpClient.getContentsOf(uri);
            } catch (HttpException e) {
                LOG.error("error getting seesaw playlist contents", e);
                return null;
            }

            if (content != null) {
                HtmlNavigator navigator = new HtmlNavigator(content);
                Series series = new Series(uri, SeesawHelper.getCurieFromLink(uri));
                
                Element seriesInfoElem = navigator.firstElementOrNull("//div[@class='information']//*[text()='About this series:']/parent::div/div");
                if (seriesInfoElem != null) {
                    String seriesInfo = SeesawHelper.getFirstTextContent(seriesInfoElem).trim();
                    series.setDescription(seriesInfo);
                }
                
                List<Element> targetLinkElements = navigator.allElementsMatching("//a[contains(@class,'targetLink')]");
                
                if (targetLinkElements.isEmpty()) {
                    addEpisode(uri, series);
                }
                else {
                    for (Element targetLinkElement : targetLinkElements) {
                        addEpisode(targetLinkElement.getAttributeValue("href"), series);
                    }
                }
                
                return series;
            } else {
                LOG.error("Unable to retrieve seesaw playlist: " + uri);
            }
        } catch (JaxenException e) {
            LOG.warn("Error retrieving all hulu brands: " + uri + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
            throw new FetchException("Unable to retrieve all hulu brands", e);
        }
        
        return null;
    }

    private void addEpisode(String uri, Series series) {
        Episode episode = itemAdapter.fetch(uri);
        if (episode != null) {
            series.getGenres().addAll(episode.getGenres());
            
            if (episode.getSeriesNumber() != null) {
                series.withSeriesNumber(episode.getSeriesNumber());
                if (series.getTitle() == null) {
                    String title = episode.getBrand() != null ? episode.getBrand()+" - " : "";
                    title+= "Series "+series.getSeriesNumber();
                    series.setTitle(title);
                }
            }
            
            if (series.getImage() == null && episode.getImage() != null) {
                series.setImage(episode.getImage());
            }
            
            series.addItem(episode);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        Matcher matcher = seesawContentPagePattern.matcher(uri);
        return matcher.matches();
    }
}
