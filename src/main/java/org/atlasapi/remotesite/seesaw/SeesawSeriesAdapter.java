package org.atlasapi.remotesite.seesaw;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Element;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawSeriesAdapter implements SiteSpecificAdapter<Series> {
    private final Pattern seesawContentPagePattern = Pattern.compile("http://www.seesaw.com/(.*)/s-[0-9]+-(.*)");

    private final SimpleHttpClient httpClient;
    private final SiteSpecificAdapter<Episode> itemAdapter;
    
    public SeesawSeriesAdapter() {
        this(HttpClients.screenScrapingClient());
    }

    public SeesawSeriesAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        itemAdapter = new SeesawItemAdapter(httpClient);
    }

    @Override
    public Series fetch(String uri) {
        try {
            String content = httpClient.getContentsOf(uri);

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
            } else {
                for (Element targetLinkElement : targetLinkElements) {
                    addEpisode(targetLinkElement.getAttributeValue("href"), series);
                }
            }

            return series;
        } catch (HttpException e) {
            throw new FetchException("Unable to retrieve series: " + uri, e);
        } catch (JaxenException e) {
            throw new FetchException("Unable to retrieve all hulu brands", e);
        }
    }

    private void addEpisode(String uri, Series series) {
        Episode episode = itemAdapter.fetch(uri);
        if (episode != null) {
            series.getGenres().addAll(episode.getGenres());

            if (episode.getSeriesNumber() != null) {
                series.withSeriesNumber(episode.getSeriesNumber());
                if (series.getTitle() == null) {
                    String title = episode.getContainer() != null ? episode.getContainer() + " - " : "";
                    title += "Series " + series.getSeriesNumber();
                    series.setTitle(title);
                }
            }

            if (series.getImage() == null && episode.getImage() != null) {
                series.setImage(episode.getImage());
            }

            series.addContents(ImmutableList.of(episode));
        }
    }

    @Override
    public boolean canFetch(String uri) {
        Matcher matcher = seesawContentPagePattern.matcher(uri);
        return matcher.matches();
    }
}
