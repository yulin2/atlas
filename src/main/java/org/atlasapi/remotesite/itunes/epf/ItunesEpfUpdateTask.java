package org.atlasapi.remotesite.itunes.epf;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.remotesite.itunes.epf.EpfHelper.curieForBrand;
import static org.atlasapi.remotesite.itunes.epf.EpfHelper.uriForBrand;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.itunes.epf.model.ArtistType;
import org.atlasapi.remotesite.itunes.epf.model.CollectionType;
import org.atlasapi.remotesite.itunes.epf.model.EpfArtist;
import org.atlasapi.remotesite.itunes.epf.model.EpfArtistCollection;
import org.atlasapi.remotesite.itunes.epf.model.EpfCollection;
import org.atlasapi.remotesite.itunes.epf.model.EpfCollectionVideo;
import org.atlasapi.remotesite.itunes.epf.model.EpfPricing;
import org.atlasapi.remotesite.itunes.epf.model.EpfVideo;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.metabroadcast.common.currency.Price;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ItunesEpfUpdateTask extends ScheduledTask {

    private final Supplier<EpfDataSet> dataSetSupplier;
    private final ContentWriter writer;

    private final ItunesCollectionSeriesExtractor seriesExtractor = new ItunesCollectionSeriesExtractor();
    private final ItunesVideoEpisodeExtractor episodeExtractor = new ItunesVideoEpisodeExtractor();
    private final AdapterLog log;
    
    public ItunesEpfUpdateTask(Supplier<EpfDataSet> dataSetSupplier, ContentWriter writer, AdapterLog log) {
        this.dataSetSupplier = dataSetSupplier;
        this.writer = writer;
        this.log = log;
    }

    @Override
    protected void runTask() {
        try {
            EpfDataSet dataSet = dataSetSupplier.get();

            reportStatus("Extracting brands..."); //brand id -> brand
            final Map<Integer, Brand> extractedBrands = extractBrands(dataSet.getArtistTable());
            
            int brands = 0;
            for (Brand brand : extractedBrands.values()) {
                writer.createOrUpdate(brand);
                reportStatus(String.format("Writing brands %s/%s", ++brands, extractedBrands.size()));
            }

            reportStatus("Extracting series..."); //series id -> series
            final BiMap<Integer, Series> extractedSeries = extractSeries(dataSet.getCollectionTable());
            
            linkBrandsAndSeries(dataSet.getArtistCollectionTable(), extractedBrands, extractedSeries);
//            reportStatus("Extracting series IDs...");
            //series id -> brand
            final Map<Integer, Brand> collectionIdBrandMap = extractCollectionIds(dataSet.getArtistCollectionTable(), extractedBrands);
            
            reportStatus("Extracting video IDs...");
            //episode id -> trackNumber/series
            final Map<Integer, SeriesVideoIdentifier> videoIdCollectionMap = extractVideoIds(dataSet.getCollectionVideoTable(), extractedSeries);
            
            int seriess = 0;
            for (Series series : extractedSeries.values()) {
                writer.createOrUpdate(series);
                reportStatus(String.format("Writing series %s/%s", ++seriess, extractedSeries.size()));
            }

            reportStatus("Extracting locations...");
            Multimap<String, Location> extractedLocations = extractLocations(dataSet, ImmutableSet.of(Countries.GB, Countries.US));
            
            reportStatus("Extracting episodes");
            Map<Integer, Episode> extractedEpisodes = extractVideos(dataSet.getVideoTable(), collectionIdBrandMap, extractedSeries, videoIdCollectionMap, extractedLocations);

            int episodes = 0;
            for (Episode episode : extractedEpisodes.values()) {
                writer.createOrUpdate(episode);
                reportStatus(String.format("Writing series %s/%s", ++episodes, extractedEpisodes.size()));
            }
            
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withDescription("Error during EPF update").withSource(getClass()));
            throw Throwables.propagate(e);
        }
    }

    private void linkBrandsAndSeries(EpfTable<EpfArtistCollection> artistCollectionTable, final Map<Integer, Brand> brands, final BiMap<Integer, Series> series) throws IOException {
        artistCollectionTable.processRows(new EpfTableRowProcessor<EpfArtistCollection, Void>() {

            @Override
            public boolean process(EpfArtistCollection row) {
                Brand brand = brands.get(row.get(EpfArtistCollection.ARTIST_ID));
                if(brand != null) {
                    Series sery = series.get(row.get(EpfArtistCollection.COLLECTION_ID));
                    if (sery != null) {
                        sery.setParent(brand);
                    }
                }
                return true;
            }

            @Override
            public Void getResult() {
                return null;
            }
        });
    }

    private Multimap<String, Location> extractLocations(final EpfDataSet dataSet, ImmutableSet<Country> countries) throws IOException {
        Iterable<Location> locations = Iterables.concat(Iterables.transform(countries, new Function<Country, Set<Location>>() {
            @Override
            public Set<Location> apply(final Country country) {
                try {
                    EpfTable<EpfPricing> pricingTable = dataSet.getPricingTable(country);
                    if (pricingTable == null) {
                        return ImmutableSet.of();
                    }
                    return pricingTable.processRows(new EpfTableRowProcessor<EpfPricing, Set<Location>>() {

                        private final ImmutableSet.Builder<Location> countryLocations = ImmutableSet.builder();

                        @Override
                        public boolean process(EpfPricing row) {
                            BigDecimal sdPrice = row.get(EpfPricing.SD_PRICE);
                            if (sdPrice != null) {
                                Location location = new Location();
                                location.setUri(row.get(EpfPricing.EPISODE_URL));
                                location.setTransportType(TransportType.APPLICATION);
                                location.setTransportSubType(TransportSubType.ITUNES);

                                Policy policy = new Policy();
                                policy.addAvailableCountry(country);
                                policy.setRevenueContract(RevenueContract.PAY_TO_BUY);

                                Currency currency = Currency.getInstance(new Locale("en", country.code()));
                                policy.setPrice(new Price(currency, sdPrice.movePointLeft(currency.getDefaultFractionDigits()).intValue()));

                                location.setPolicy(policy);

                                countryLocations.add(location);
                            }
                            return isRunning();
                        }

                        @Override
                        public Set<Location> getResult() {
                            return countryLocations.build();
                        }
                    });
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        }));
        
        return Multimaps.index(locations, new Function<Location, String>() {
            
            private final String commonPrefix = "http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?";
            private final Pattern urlIdPattern = Pattern.compile(Pattern.quote(commonPrefix) + "uo=\\d+&i=(\\d+)&id=\\d+");
            
            @Override
            public String apply(Location input) {
                return extractIdFrom(input.getUri());
            }

            private String extractIdFrom(String uri) {
                Matcher matcher = urlIdPattern.matcher(uri);
                if(matcher.matches()) {
                    return matcher.group(1);
                }
                return "";
            }
        });
    }

    private Map<Integer, Episode> extractVideos(EpfTable<EpfVideo> videosTable, final Map<Integer, Brand> collectionIdBrandMap,
            final BiMap<Integer, Series> extractedSeries, final Map<Integer, SeriesVideoIdentifier> videoIdCollectionMap, final Multimap<String, Location> extractedLocations) throws IOException {
        return videosTable.processRows(new EpfTableRowProcessor<EpfVideo, Map<Integer,Episode>>() {

            ImmutableMap.Builder<Integer, Episode> results = ImmutableMap.builder();
            
            @Override
            public boolean process(EpfVideo row) {
                Integer videoId = row.get(EpfVideo.VIDEO_ID);
                
                SeriesVideoIdentifier seriesId = videoIdCollectionMap.get(videoId);
                if (videoId != null) {
                    Brand brand = collectionIdBrandMap.get(extractedSeries.inverse().get(seriesId.series()));
                    results.put(videoId, episodeExtractor.extract(new ItunesEpfVideoSource(row, brand, seriesId.series(), seriesId.trackNumber(), extractedLocations.get(String.valueOf(videoId)))));
                }
                return isRunning();
            }

            @Override
            public Map<Integer, Episode> getResult() {
                return results.build();
            }
        });
    }

    private Map<Integer,SeriesVideoIdentifier> extractVideoIds(EpfTable<EpfCollectionVideo> cvTable, final Map<Integer, Series> extractedSeries) throws IOException {
        return cvTable.processRows(new EpfTableRowProcessor<EpfCollectionVideo, Map<Integer, SeriesVideoIdentifier>>() {

            private final ImmutableBiMap.Builder<Integer, SeriesVideoIdentifier> videoIdCollectionMap = ImmutableBiMap.<Integer, SeriesVideoIdentifier>builder();
            
            @Override
            public boolean process(EpfCollectionVideo row) {
                Series series = extractedSeries.get(row.get(EpfCollectionVideo.COLLECTION_ID));
                if(series != null) {
                    videoIdCollectionMap.put(row.get(EpfCollectionVideo.VIDEO_ID), 
                            new SeriesVideoIdentifier(series.withSeriesNumber(row.get(EpfCollectionVideo.VOLUME_NUMBER)), row.get(EpfCollectionVideo.TRACK_NUMBER)));
                }
                return false;
            }

            @Override
            public Map<Integer, SeriesVideoIdentifier> getResult() {
                return videoIdCollectionMap.build();
            }
            
        });
    }

    private BiMap<Integer, Series> extractSeries(EpfTable<EpfCollection> collTable) throws IOException {
        return collTable.processRows(new EpfTableRowProcessor<EpfCollection, BiMap<Integer, Series>>() {

            ImmutableBiMap.Builder<Integer, Series> results = ImmutableBiMap.builder();
            
            @Override
            public boolean process(EpfCollection row) {
                Integer collectionId = row.get(EpfCollection.COLLECTION_ID);
                
                if (CollectionType.TV_SEASON.equals(row.get(EpfCollection.COLLECTION_TYPE_ID))) {
                    results.put(collectionId, seriesExtractor.extract(row));
                }
                
                return isRunning();
            }
            
            @Override
            public BiMap<Integer, Series> getResult() {
                return results.build();
            }
        });
    }

    private BiMap<Integer, Brand> extractCollectionIds(EpfTable<EpfArtistCollection> acTable, final Map<Integer, Brand> extractedBrands) throws IOException {
        return acTable.processRows(new EpfTableRowProcessor<EpfArtistCollection,BiMap<Integer,Brand>>() {

            private final ImmutableBiMap.Builder<Integer, Brand> collectionIdBrandMap = ImmutableBiMap.<Integer, Brand>builder();

            @Override
            public boolean process(EpfArtistCollection row) {
                Brand brand = extractedBrands.get(row.get(EpfArtistCollection.ARTIST_ID));
                if(brand != null && row.get(EpfArtistCollection.IS_PRIMARY_ARTIST)) {
                    collectionIdBrandMap.put(row.get(EpfArtistCollection.COLLECTION_ID), brand);
                }
                return isRunning();
            }

            @Override
            public BiMap<Integer,Brand> getResult() {
                return collectionIdBrandMap.build();
            }
        });
    }

    private Map<Integer, Brand> extractBrands(EpfTable<EpfArtist> artistTable) throws IOException {
        return artistTable.processRows(new EpfTableRowProcessor<EpfArtist,Map<Integer,Brand>>() {
           
            private final ImmutableMap.Builder<Integer, Brand> extractedBrands = ImmutableMap.builder();
            
            @Override
            public boolean process(EpfArtist row) {
                if(row.get(EpfArtist.IS_ACTUAL_ARTIST) && ArtistType.TV_SHOW.equals(row.get(EpfArtist.ARTIST_TYPE_ID))) {
                    Brand extractedBrand = extract(row);
                    extractedBrands.put(row.get(EpfArtist.ARTIST_ID), extractedBrand);
                }
                return isRunning();
            }

            private Brand extract(EpfArtist row) {
                Integer rowId = row.get(EpfArtist.ARTIST_ID);
                Brand brand = new Brand(uriForBrand(rowId), curieForBrand(rowId), Publisher.ITUNES);
                brand.setTitle(row.get(EpfArtist.NAME));
                return brand;
            }

            @Override
            public Map<Integer,Brand> getResult() {
                return extractedBrands.build();
            }
        });
    }
    

}
