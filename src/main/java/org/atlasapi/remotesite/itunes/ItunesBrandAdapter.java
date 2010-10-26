package org.atlasapi.remotesite.itunes;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.currency.Price;
import com.metabroadcast.common.http.SimpleHttpClient;

public class ItunesBrandAdapter implements SiteSpecificAdapter<Brand> {
    
    GenreMap genreMap = new ItunesGenreMap(); 
    Pattern brandUriPattern = Pattern.compile("http://itunes\\.apple\\.com/WebObjects/MZStore\\.woa/wa/viewTVShow\\?id=([0-9]+)(&.+)*");
    Pattern seasonNumberPattern = Pattern.compile(".*Season ([0-9]+)$");
    Pattern seriesNumberPattern = Pattern.compile(".*Series ([0-9]+)$");
    
    private static final String LOOKUP_URL_BASE = "http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStoreServices.woa/wa/wsLookup?limit=200&country=gb&media=tvShow";
    
    private final SimpleHttpClient client;
    private final AdapterLog log;

    public ItunesBrandAdapter(SimpleHttpClient client, AdapterLog log) {
        this.client = client;
        this.log = log;
    }
    
    @Override
    public Brand fetch(String uri) {
        Matcher brandUriMatcher = brandUriPattern.matcher(uri);
        brandUriMatcher.matches();
        String brandId = brandUriMatcher.group(1);
        
        Brand brand = findBrand(brandId);
        Map<Long, Series> series = findSeries(brandId);
        List<Episode> episodes = findEpisodes(brandId, series);
        
        brand.setItems(episodes);
        
        return brand;
    }
    
    private Brand findBrand(String brandId) {
        String brandSearchUri = LOOKUP_URL_BASE + "&id=" + brandId;
        try {
            String contents = client.getContentsOf(brandSearchUri);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(contents, JsonNode.class);
            JsonNode resultsNode = rootNode.path("results");
            
            if (resultsNode.size() > 0) {
                JsonNode brandNode = resultsNode.iterator().next();
                
                if (isBrandNode(brandNode)) {
                    String brandUri = brandNode.path("artistLinkUrl").getTextValue();
                    long artistId = brandNode.path("artistId").getNumberValue().longValue();
                    Brand brand = new Brand(brandUri, getCurie(artistId), Publisher.ITUNES);
                    brand.setTitle(brandNode.path("artistName").getTextValue());
                    
                    String primaryGenre = brandNode.path("primaryGenreName").getTextValue();
                    Set<String> genres = genreMap.mapRecognised(ImmutableSet.of(getGenreUri(primaryGenre)));
                    brand.setGenres(genres);
                    
                    return brand;
                }
            }
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class).withUri(brandSearchUri));
        }
        
        return null;
    }
    
    private Map<Long, Series> findSeries(String brandId) {
        Map<Long, Series> seriesIdToSeries = Maps.newHashMap();
        String seriesSearchUri = LOOKUP_URL_BASE + "&id=" + brandId + "&entity=tvSeason";
        
        try {
            String contents = client.getContentsOf(seriesSearchUri);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(contents, JsonNode.class);
            JsonNode resultsNode = rootNode.path("results");
            
            for (JsonNode seriesNode : resultsNode) {
                if (isSeriesNode(seriesNode)) {
                    String seriesTitle = seriesNode.path("collectionName").getTextValue();
                    Integer seriesNumber = getSeriesNumber(seriesTitle);
                    long seriesId = seriesNode.path("collectionId").getNumberValue().longValue();
                    Series series = null;
                    
                    if (seriesNumber != null) {
                        String seriesUrl = seriesNode.path("collectionViewUrl").getTextValue();
                        
                        String seriesThumbnail = seriesNode.path("artworkUrl100").getTextValue();
                        String seriesGenre = seriesNode.path("primaryGenreName").getTextValue();
                        
                        JsonNode priceNode = seriesNode.path("collectionPrice");
                        if (priceNode != null && priceNode.getNumberValue().doubleValue() > 0) {
                            Number seriesPrice = priceNode.getNumberValue();
                            String currency = seriesNode.path("currency").getTextValue();
                            Price price = new Price(Currency.getInstance(currency), seriesPrice.doubleValue());
                        }
                        
                        series = new Series(seriesUrl, getCurie(seriesId));
                        series.setTitle(seriesTitle);
                        series.withSeriesNumber(seriesNumber);
                        series.setPublisher(Publisher.ITUNES);
                        series.setThumbnail(seriesThumbnail);
                        series.setImage(seriesThumbnail);
                        series.setGenres(genreMap.mapRecognised(ImmutableSet.of(getGenreUri(seriesGenre))));
                        
                        
                    }
                    seriesIdToSeries.put(seriesId, series);
                }
            }
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class).withUri(seriesSearchUri));
        }
        
        return seriesIdToSeries;
    }
    
    private Integer getSeriesNumber(String seriesTitle) {
        Matcher seriesNumberMatcher = seriesNumberPattern.matcher(seriesTitle);
        if (seriesNumberMatcher.matches()) {
            return Integer.valueOf(seriesNumberMatcher.group(1));
        }
        
        Matcher seasonNumberMatcher = seasonNumberPattern.matcher(seriesTitle);
        if (seasonNumberMatcher.matches()) {
            return Integer.valueOf(seasonNumberMatcher.group(1));
        }
        
        return null;
    }
    
    private List<Episode> findEpisodes(String brandId, Map<Long, Series> seriesIdToSeries) {
        List<Episode> episodes = Lists.newArrayList();
        
        for(Long seriesId : seriesIdToSeries.keySet()) {
            String episodesSearchUri = LOOKUP_URL_BASE + "&id=" + seriesId + "&entity=tvEpisode";
            try {
                String contents = client.getContentsOf(episodesSearchUri);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readValue(contents, JsonNode.class);
                JsonNode resultsNode = rootNode.path("results");
                
                for (JsonNode episodeNode : resultsNode) {
                    if (isEpisodeNode(episodeNode)) {
                        Episode episode = extractEpisode(episodeNode);
                        
                        if (episode != null) {
                            episodes.add(episode);
                            
                            Series series = seriesIdToSeries.get(seriesId);
                            if (series != null) {
                                if (series.getSeriesNumber() != null) {
                                    episode.setSeriesNumber(series.getSeriesNumber());
                                }
                                series.addItem(episode);
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class).withUri(episodesSearchUri));
            }
        }
        
        return episodes;
    }
    
    private Episode extractEpisode(JsonNode episodeNode) {
        try {
            String episodeUri = episodeNode.path("trackViewUrl").getTextValue();
            long episodeId = episodeNode.path("trackId").getNumberValue().longValue();
            
            Episode episode = new Episode(episodeUri, getCurie(episodeId), Publisher.ITUNES);
            episode.setTitle(episodeNode.path("trackName").getTextValue());
            
            
            String imageUrl = episodeNode.path("artworkUrl30").getTextValue();
            episode.setImage(imageUrl);
            
            String thumbnailUrl = episodeNode.path("artworkUrl100").getTextValue();
            episode.setThumbnail(thumbnailUrl);
            
            Integer episodeNumber = episodeNode.path("trackNumber").getNumberValue().intValue();
            episode.setEpisodeNumber(episodeNumber);
            
            String primaryGenre = episodeNode.path("primaryGenreName").getTextValue();
            episode.setGenres(genreMap.mapRecognised(ImmutableSet.of(getGenreUri(primaryGenre))));
            
            long showId = episodeNode.path("artistId").getNumberValue().longValue();
            long seriesId = episodeNode.path("collectionId").getNumberValue().longValue();
            long trackTimeMillis = episodeNode.path("trackTimeMillis").getNumberValue().longValue();
            
            JsonNode priceNode = episodeNode.path("trackPrice");
            
            Version version = new Version();
            version.setDuration(new Duration(trackTimeMillis));
            version.setProvider(Publisher.ITUNES);
            Encoding encoding = new Encoding();
            
            Policy policy = new Policy();
            policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
            if (priceNode != null && priceNode.getNumberValue().doubleValue() > 0) {
                double amount = priceNode.getNumberValue().doubleValue();
                String currencyCode = episodeNode.path("currency").getTextValue();
                policy.setPrice(new Price(Currency.getInstance(currencyCode), amount));
                policy.setRevenueContract(RevenueContract.PAY_TO_RENT);
            }
            else {
                policy.setRevenueContract(RevenueContract.FREE_TO_VIEW);
            }
            
            Location linkLocation = new Location();
            linkLocation.setUri(episodeUri);
            linkLocation.setTransportType(TransportType.LINK);
            linkLocation.setTransportSubType(TransportSubType.HTTP);
            linkLocation.setPolicy(policy);
            
            Location appLocation = new Location();
            appLocation.setUri(getItunesLink(seriesId, episodeId));
            appLocation.setTransportType(TransportType.APPLICATION);
            appLocation.setTransportSubType(TransportSubType.ITUNES);
            appLocation.setPolicy(policy);
            
            encoding.addAvailableAt(appLocation);
            encoding.addAvailableAt(linkLocation);
            version.addManifestedAs(encoding);
            episode.setVersions(ImmutableSet.of(version));
            
            return episode;
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class));
        }
        
        return null;
    }
    
    private String getItunesLink(long seriesId, long episodeId) {
        return "itms://phobos.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?i=" + episodeId + "&id=" + seriesId;
        //itms://itunes.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?i=293550579&id=293360601
        //itms://phobos.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?id=102808898ign-mscache=1.
        //itms://itunes.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?i=293550579id=293360601s=143444ign-mpt=uo%3D4ign-mscache=1.
    }
    
    private String getCurie(long entityId) {
        return "itunes:" + entityId;
    }
    
    private long getIdFromCurie(String curie) {
        return Long.valueOf(curie.substring("itunes:".length()));
    }
    
    private String getGenreUri(String genreName) {
        return "http://itunes.apple.com/genres/" + genreName.toLowerCase();
    }
    
    private boolean isEpisodeNode(JsonNode node) {
        String wrapperType = node.get("wrapperType").getTextValue();
        if ("track".equals(wrapperType)) {
            String artistType = node.path("kind").getTextValue();
            if ("tv-episode".equals(artistType)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isBrandNode(JsonNode node) {
        String wrapperType = node.path("wrapperType").getTextValue();
        if ("artist".equals(wrapperType)) {
            String artistType = node.path("artistType").getTextValue();
            if ("TV Show".equals(artistType)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSeriesNode(JsonNode node) {
        String wrapperType = node.path("wrapperType").getTextValue();
        if ("collection".equals(wrapperType)) {
            String collectionType = node.path("collectionType").getTextValue();
            if ("TV Season".equals(collectionType)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean canFetch(String uri) {
        return brandUriPattern.matcher(uri).matches();
    }

}
