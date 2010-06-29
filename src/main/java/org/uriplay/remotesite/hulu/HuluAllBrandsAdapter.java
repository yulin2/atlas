package org.uriplay.remotesite.hulu;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.MutableContentStore;
import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.HttpClients;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.http.SimpleHttpClient;

public class HuluAllBrandsAdapter implements SiteSpecificAdapter<Playlist> {

    private static final String URL = "http://www.hulu.com/browse/alphabetical/episodes";
    private final SimpleHttpClient httpClient;
    private final SiteSpecificAdapter<Brand> brandAdapter;
    static final Log LOG = LogFactory.getLog(HuluAllBrandsAdapter.class);
    private MutableContentStore contentStore;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public HuluAllBrandsAdapter() {
        this(HttpClients.webserviceClient(), new HuluBrandAdapter());
    }

    public HuluAllBrandsAdapter(SiteSpecificAdapter<Brand> brandAdapter) {
        this(HttpClients.webserviceClient(), brandAdapter);
    }

    public HuluAllBrandsAdapter(SimpleHttpClient httpClient, SiteSpecificAdapter<Brand> brandAdapter) {
        this.httpClient = httpClient;
        this.brandAdapter = brandAdapter;
    }

    public void setContentStore(MutableContentStore contentStore) {
        this.contentStore = contentStore;
    }

    @Override
    public Playlist fetch(String uri) {
        try {
            LOG.info("Retrieving all Hulu brands");

            String content = httpClient.getContentsOf(uri);
            HtmlNavigator navigator = new HtmlNavigator(content);

            List<Element> elements = navigator.allElementsMatching("//a[@rel='nofollow']");
            for (Element element : elements) {
                String brandUri = element.getAttributeValue("href");
                if (brandAdapter.canFetch(brandUri)) {
                    if (contentStore != null) {
                        executor.execute(new BrandHydratingJob(brandUri));
                    }
                }
            }

            // Returning empty playlist
            return new Playlist(URL, "hulu:all_brands");
        } catch (Exception e) {
            throw new FetchException("Unable to retrieve all hulu brands", e);
        }
    }

    class BrandHydratingJob implements Runnable {

        private final String uri;

        public BrandHydratingJob(String uri) {
            this.uri = uri;
        }

        public void run() {
            try {
                Brand brand = brandAdapter.fetch(uri);
                contentStore.createOrUpdatePlaylist(brand, false);
            } catch (Exception e) {
                LOG.warn("Error retrieving Hulu brand: " + uri + " while retrieving all brands with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
            }
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return URL.equals(uri);
    }

    public static class HuluAllBrandsCanonicaliser implements Canonicaliser {
        @Override
        public String canonicalise(String uri) {
            return URL.equals(uri) ? uri : null;
        }
    }
}
