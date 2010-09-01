package org.atlasapi.remotesite.seesaw;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Element;

import com.google.common.collect.Lists;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class SeesawAtoZBrandsAdapter implements SiteSpecificAdapter<Playlist> {
    private static final String URL = "http://www.seesaw.com/AtoZ";
    static final Log LOG = LogFactory.getLog(SeesawAtoZBrandsAdapter.class);
    private final SimpleHttpClient httpClient;
    private final SiteSpecificAdapter<Playlist> playlistAdapter;
    
    public SeesawAtoZBrandsAdapter() {
        this(HttpClients.screenScrapingClient());
    }
    
    public SeesawAtoZBrandsAdapter(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        this.playlistAdapter = new SeesawPlaylistAdapter(httpClient);
    }

    @Override
    public Playlist fetch(String uri) {
        try {
            LOG.info("Retrieving all Seesaw brands");
            System.out.println("Attempting to load SeeSaw brands " + uri);

            Playlist globalPlaylist = new Playlist();
            String content;
            try {
                content = httpClient.getContentsOf(uri);
            } catch (HttpException e) {
                LOG.warn("Error retrieving seesaw brands: " + uri + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
                return null;
            }

            if (content != null) {
                HtmlNavigator navigator = new HtmlNavigator(content);
                // these are all the links to viewable series / brand.. can we use these instead?
                List<String> badUrls = Lists.newArrayList();
                
                List<Element> brandElements = navigator.allElementsMatching("//div[@class='programmeContent']/div[@class='toggleLI' or @class='clickableElement']");
                
                for (Element brandElement : brandElements) {
                    Element titleElement = navigator.firstElementOrNull("div[@class='header narrow']", brandElement);
                    String brandTitle = SeesawHelper.getFirstTextContent(titleElement);
                    
                    Element genreElement = navigator.firstElementOrNull("div[@class='genre']", brandElement);
                    String brandGenre = SeesawHelper.getFirstTextContent(genreElement);
                    
                    Element actionContainer = navigator.firstElementOrNull("div[@class='action']", brandElement);
                    Element actionLink = navigator.firstElementOrNull("a[@class='seeLink']", actionContainer);
                    
                    List<String> episodeContainerLinks = Lists.newArrayList();
                    
                    String brandLink = null;
                    if (actionLink != null) {
                        brandLink = actionLink.getAttributeValue("href");
                        episodeContainerLinks.add(brandLink);
                    }
                    
                    if (brandLink == null) {
                        List<Element> seriesElements = navigator.allElementsMatching("div[@class='moreInfo']/ul/li/div[@class='header narrow']/*/a", brandElement);
                        
                        for (Element seriesElement : seriesElements) {
                            String seriesTitle = seriesElement.getAttributeValue("title");
                            String seriesLink = seriesElement.getAttributeValue("href");
                            episodeContainerLinks.add(seriesLink);
                        }
                    }
                    
                    Brand brand = new Brand();
                    brand.setTitle(brandTitle);
                    brand.setPublisher(Publisher.SEESAW);
                    
                    for (String episodeContainerLink : episodeContainerLinks) {
                        if (episodeContainerLink.startsWith("http://")) {
                            if (brand.getCanonicalUri() == null) {
                                String canonicalUri = SeesawHelper.getCanonicalUriFromLink(episodeContainerLink);
                                System.out.println("brand uri = " + canonicalUri);
                                brand.setCanonicalUri(canonicalUri);
                            }
                            
                            if (brand.getCurie() == null) {
                                brand.setCurie(SeesawHelper.getCurieFromLink(episodeContainerLink));
                            }
                            
                            Playlist playlist = playlistAdapter.fetch(episodeContainerLink);
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
                        else {
                            badUrls.add(episodeContainerLink);
                        }
                    }
                    
                    globalPlaylist.addPlaylist(brand);
                }
                
                return globalPlaylist;
            } else {
                LOG.error("Unable to retrieve seesaw brands: " + uri);
            }
            
            // Returning empty playlist
            //new Playlist(URL, "hulu:all_brands", Publisher.HULU);
        } catch (JaxenException e) {
            LOG.warn("Error retrieving all hulu brands: " + uri + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
            e.printStackTrace();
            throw new FetchException("Unable to retrieve all hulu brands", e);
        }
        return null;
    }
    
    
    
    @Override
    public boolean canFetch(String uri) {
        return uri.startsWith(URL);
    }

}
