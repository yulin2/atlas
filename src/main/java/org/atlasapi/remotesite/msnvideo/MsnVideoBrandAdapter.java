package org.atlasapi.remotesite.msnvideo;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.xml.SimpleXmlNavigator;
import org.jdom.Element;

import com.google.common.collect.Lists;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.url.UrlEncoding;

public class MsnVideoBrandAdapter implements SiteSpecificAdapter<Brand> {
    
    private static final Pattern brandUriPattern = Pattern.compile("http://video.uk.msn.com/browse/tv-shows/show\\?tag=([\\w\\+\\-%]+)");
    
    private final SimpleHttpClient client;
    private final AdapterLog log;

    private final SiteSpecificAdapter<Episode> episodeAdapter;

    public MsnVideoBrandAdapter(SimpleHttpClient client, AdapterLog log, SiteSpecificAdapter<Episode> episodeAdapter) {
        this.client = client;
        this.log = log;
        this.episodeAdapter = episodeAdapter;
    }

    @Override
    public Brand fetch(String uri) {
        try {
            Matcher brandUriMatcher = brandUriPattern.matcher(uri);
            brandUriMatcher.matches();
            String encodedTag = brandUriMatcher.group(1);
            
            Brand brand = new Brand(uri, "msn:" + encodedTag, Publisher.MSN_VIDEO);
            List<Episode> episodes = Lists.newArrayList();
            
            for (int page = 1; page < 1000; page++) {
                SimpleXmlNavigator navigator = getNavigatorForShow(encodedTag, page++);
                
                List<Element> itemElements = navigator.allElementsMatching("//div[@class='item']");
                
                if (brand.getTitle() == null) {
                    brand.setTitle(navigator.firstElementOrNull("//a[@data-instname='Series' ]").getText());
                }
                
                if (itemElements.isEmpty()) {
                    break;
                }
                
                for (Element itemElement : itemElements) {
                    String episodeUri = navigator.firstElementOrNull("div/span/a[contains(@class, 'playerUrl')]", itemElement).getAttributeValue("href");
                    if (episodeAdapter.canFetch(episodeUri)) {
                        Episode episode = episodeAdapter.fetch(episodeUri);
                        if (episode != null) {
                            String episodeImage = navigator.firstElementOrNull("div/span/a/img", itemElement).getAttributeValue("src");
                            if (brand.getImage() == null) {
                                brand.setImage(episodeImage);
                            }
                            episode.setImage(episodeImage);
                            episode.setThumbnail(episodeImage);
                            episodes.add(episode);
                        }
                    }
                    else {
                        log.record(new AdapterLogEntry(Severity.INFO).withDescription("Item adapter could not fetch " + episodeUri).withSource(MsnVideoBrandAdapter.class).withUri(uri));
                    }
                    
                }
            }
            brand.setContents(episodes);
            return brand;
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(MsnVideoBrandAdapter.class).withUri(uri));
        }
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        return brandUriPattern.matcher(uri).matches();
    }
    
    private SimpleXmlNavigator getNavigatorForShow(String encodedTag, int pageNumber) throws UnsupportedEncodingException, HttpException {
        String baseUrl = "http://video.uk.msn.com/browse/tv-shows/show" +
                "?tag=" + encodedTag +
                "&rt=ajax" +
                "&videocontent=" + 
                UrlEncoding.encode("<videoQuery>" +
                  "<videoFilter>" +
                      "<type>Tag</type>" +
                      "<tags>" +
                          "<tag namespace=\"series_title\">") + encodedTag + UrlEncoding.encode("</tag>" +
                          "<tag namespace=\"videotype\">tv</tag>" +
                          "<tag namespace=\"videotypevariant\">full episode</tag>" +
                      "</tags>" +
                      "<source>Msn</source>" +
                      "<dataCatalog>Video</dataCatalog>" +
                      "<relatedAlgorithm>0</relatedAlgorithm>" +
                      "<safetyFilter>Moderate</safetyFilter>" +
                  "</videoFilter>" +
                  "<videoSort>" +
                      "<sortDirection>Descending</sortDirection>" +
                      "<sortField>Date</sortField>" +
                      "</videoSort>" +
                  "</videoQuery>") + 
                  "&id=ux1_5_1" +
                  "&currentPage=" + pageNumber;
        
       String content = client.getContentsOf(baseUrl);        
       return new SimpleXmlNavigator(content);
    }
}
