//package org.atlasapi.remotesite.hulu;
//
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import javax.annotation.PreDestroy;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.atlasapi.media.entity.Brand;
//import org.atlasapi.persistence.content.ContentWriter;
//import org.atlasapi.remotesite.FetchException;
//import org.atlasapi.remotesite.HttpClients;
//import org.atlasapi.remotesite.SiteSpecificAdapter;
//import org.atlasapi.remotesite.html.HtmlNavigator;
//import org.jdom.Element;
//
//import com.metabroadcast.common.http.HttpException;
//import com.metabroadcast.common.http.SimpleHttpClient;
//
//public class HuluAllBrandsUpdater implements Runnable {
//
//    private static final String URL = "http://www.hulu.com/browse/alphabetical/episodes";
//    
//    private final SimpleHttpClient httpClient;
//    private final SiteSpecificAdapter<Brand> brandAdapter;
//    
//    static final Log LOG = LogFactory.getLog(HuluAllBrandsUpdater.class);
//    private ContentWriter contentStore;
//    
//    private final ExecutorService executor;
//
//    public HuluAllBrandsUpdater() {
//        this( new HuluBrandAdapter());
//    }
//
//    public HuluAllBrandsUpdater(SiteSpecificAdapter<Brand> brandAdapter) {
//        this(HttpClients.screenScrapingClient(), brandAdapter, Executors.newFixedThreadPool(2));
//    }
//
//    public HuluAllBrandsUpdater(SimpleHttpClient httpClient, SiteSpecificAdapter<Brand> brandAdapter, ExecutorService executor) {
//        this.httpClient = httpClient;
//        this.brandAdapter = brandAdapter;
//		this.executor = executor;
//    }
//
//    public void setContentStore(ContentWriter contentStore) {
//        this.contentStore = contentStore;
//    }
//
//    @Override
//    public void run() {
//        try {
//            LOG.info("Retrieving all Hulu brands");
//
//            String content = null;
//
//            for (int i = 0; i < 5; i++) {
//                try {
//                    content = httpClient.getContentsOf(URL);
//                    if (content != null) {
//                        break;
//                    }
//                } catch (HttpException e) {
//                    LOG.warn("Error retrieving all hulu brands: " + URL + " attempt " + i + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
//                }
//            }
//
//            if (content != null) {
//                HtmlNavigator navigator = new HtmlNavigator(content);
//
//                List<Element> elements = navigator.allElementsMatching("//div[@id='show_list_hiden']/a");
//                for (Element element : elements) {
//                    String brandUri = element.getAttributeValue("href");
//                    if (brandAdapter.canFetch(brandUri)) {
//                    	executor.execute(new BrandHydratingJob(brandUri));
//                    }
//                }
//            } else {
//                LOG.error("Unable to retrieve all hulu brands: " + URL);
//            }
//
//        } catch (Exception e) {
//            LOG.warn("Error retrieving all hulu brands: " + URL + " with message: " + e.getMessage() + " with cause: " + e.getCause().getMessage());
//            throw new FetchException("Unable to retrieve all hulu brands", e);
//        }
//    }
//    
//    @PreDestroy
//    public void destroy() {
//        executor.shutdown();
//    }
//
//    class BrandHydratingJob implements Runnable {
//
//        private final String uri;
//
//        public BrandHydratingJob(String uri) {
//            this.uri = uri;
//        }
//
//        public void run() {
//            try {
//                Brand brand = brandAdapter.fetch(uri);
//                contentStore.createOrUpdate(brand);
//            } catch (Exception e) {
//                LOG.warn("Error retrieving Hulu brand: " + uri + " while retrieving all brands with message: " + e.getMessage());
//            }
//        }
//    }
//}
