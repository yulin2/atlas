package org.atlasapi.remotesite.bbc.products;

import com.google.common.base.Optional;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.currency.Price;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.product.Product;
import org.atlasapi.media.product.ProductLocation;
import org.atlasapi.media.product.ProductStore;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.s3.S3Client;

/**
 */
public class BBCProductsProcessor {

    static final String PRODUCTS = "products.txt";
    static final String LOCATIONS = "locations.txt";
    static final String BRANDS = "brands.txt";
    static final String SERIES = "series.txt";
    static final String EPISODES = "episodes.txt";

    public void process(S3Client client, ProductStore productStore) throws Exception {
        BufferedReader products = loadProducts(client);
        BufferedReader locations = loadLocations(client);
        BufferedReader brands = loadBrands(client);
        BufferedReader series = loadSeries(client);
        BufferedReader episodes = loadEpisodes(client);

        String currentProduct = products.readLine();
        String currentLocation = locations.readLine();
        String currentBrand = brands.readLine();
        String currentSeries = series.readLine();
        String currentEpisode = episodes.readLine();
        while (currentProduct != null) {
            Long productId = Long.parseLong(currentProduct);
            String productTitle = products.readLine();
            String productDescription = products.readLine();
            String productGtin = products.readLine();
            String productYear = products.readLine();
            String productType = products.readLine();
            String productImage = products.readLine();
            String productThumbnail = products.readLine();

            Product product = productStore.productForSourceIdentified(Publisher.BBC, productId.toString()).orNull();
            if (product == null) {
                product = new Product();
            }

            product.setCanonicalUri(productId.toString());
            product.setTitle(productTitle);
            product.setGtin(productGtin);
            product.setDescription(productDescription);
            product.setYear(Integer.parseInt(productYear == null || productYear.isEmpty() ? "0" : productYear));
            product.setType(Product.Type.fromString(productType).orNull());
            product.setImage(Optional.fromNullable(productImage).orNull());
            product.setThumbnail(Optional.fromNullable(productThumbnail).orNull());
            product.setPublisher(Publisher.BBC);

            Set<ProductLocation> collectedLocations = new HashSet<ProductLocation>();
            while (currentLocation != null && Long.parseLong(currentLocation) == productId) {
                String locationUri = locations.readLine();
                String locationPrice = locations.readLine();
                String locationShippingPrice = locations.readLine();
                String locationAvailability = locations.readLine();

                ProductLocation location = ProductLocation.builder(locationUri).
                        withAvailability(locationAvailability).
                        withPrice(new Price(Currency.getInstance(Locale.UK), Double.parseDouble(locationPrice))).
                        withShippingPrice(new Price(Currency.getInstance(Locale.UK), Double.parseDouble(locationShippingPrice))).
                        build();
                collectedLocations.add(location);

                currentLocation = locations.readLine();
            }
            product.setLocations(collectedLocations);

            List<String> contents = new LinkedList<String>();
            while (currentBrand != null && Long.parseLong(currentBrand) == productId) {
                String brandPid = brands.readLine();
                if (!brandPid.isEmpty()) {
                    contents.add(BbcFeeds.slashProgrammesUriForPid(brandPid));
                }
                currentBrand = brands.readLine();
            }
            while (currentSeries != null && Long.parseLong(currentSeries) == productId) {
                String seriesPid = series.readLine();
                if (!seriesPid.isEmpty()) {
                    contents.add(BbcFeeds.slashProgrammesUriForPid(seriesPid));
                }
                currentSeries = series.readLine();
            }
            while (currentEpisode != null && Long.parseLong(currentEpisode) == productId) {
                String episodePid = episodes.readLine();
                if (!episodePid.isEmpty()) {
                    contents.add(BbcFeeds.slashProgrammesUriForPid(episodePid));
                }
                currentEpisode = episodes.readLine();
            }
            product.setContent(contents);

            productStore.store(product);

            currentProduct = products.readLine();
        }
    }

    private BufferedReader loadEpisodes(S3Client client) throws IOException {
        File localEpisodesFile = File.createTempFile(BBCProductsUpdater.S3_BUCKET, EPISODES);
        client.getAndSaveIfUpdated(EPISODES, localEpisodesFile, Maybe.<File>nothing());

        BufferedReader reader = new BufferedReader(new FileReader(localEpisodesFile));
        for (int i = 0; i < 2; i++) {
            reader.readLine();
        }

        return reader;
    }

    private BufferedReader loadSeries(S3Client client) throws IOException {
        File localSeriesFile = File.createTempFile(BBCProductsUpdater.S3_BUCKET, SERIES);
        client.getAndSaveIfUpdated(SERIES, localSeriesFile, Maybe.<File>nothing());

        BufferedReader reader = new BufferedReader(new FileReader(localSeriesFile));
        for (int i = 0; i < 2; i++) {
            reader.readLine();
        }

        return reader;
    }

    private BufferedReader loadBrands(S3Client client) throws IOException {
        File localBrandsFile = File.createTempFile(BBCProductsUpdater.S3_BUCKET, BRANDS);
        client.getAndSaveIfUpdated(BRANDS, localBrandsFile, Maybe.<File>nothing());

        BufferedReader reader = new BufferedReader(new FileReader(localBrandsFile));
        for (int i = 0; i < 2; i++) {
            reader.readLine();
        }

        return reader;
    }

    private BufferedReader loadLocations(S3Client client) throws IOException {
        File localLocationsFile = File.createTempFile(BBCProductsUpdater.S3_BUCKET, LOCATIONS);
        client.getAndSaveIfUpdated(LOCATIONS, localLocationsFile, Maybe.<File>nothing());

        BufferedReader reader = new BufferedReader(new FileReader(localLocationsFile));
        for (int i = 0; i < 5; i++) {
            reader.readLine();
        }

        return reader;
    }

    private BufferedReader loadProducts(S3Client client) throws IOException {
        File localProductsFile = File.createTempFile(BBCProductsUpdater.S3_BUCKET, PRODUCTS);
        client.getAndSaveIfUpdated(PRODUCTS, localProductsFile, Maybe.<File>nothing());

        BufferedReader reader = new BufferedReader(new FileReader(localProductsFile));
        for (int i = 0; i < 8; i++) {
            reader.readLine();
        }

        return reader;
    }
}
