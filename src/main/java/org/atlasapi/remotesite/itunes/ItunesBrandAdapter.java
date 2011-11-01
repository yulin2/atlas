package org.atlasapi.remotesite.itunes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.itunes.ItunesAdapterHelper.ItunesRegion;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

public class ItunesBrandAdapter implements SiteSpecificAdapter<Brand> {
    
    GenreMap genreMap = new ItunesGenreMap(); 
    Pattern brandUriPattern = Pattern.compile("http://itunes\\.apple\\.com/WebObjects/MZStore\\.woa/wa/viewTVShow\\?id=([0-9]+)(&.+)*");
    
    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ItunesAdapterHelper helper;
    private final ItunesSeriesFinder seriesFinder;
    private final ItunesEpisodesFinder episodeFinder;

    public ItunesBrandAdapter(SimpleHttpClient client, AdapterLog log, ItunesAdapterHelper helper, ItunesSeriesFinder seriesFinder, ItunesEpisodesFinder episodeFinder) {
        this.client = client;
        this.log = log;
        this.helper = helper;
        this.seriesFinder = seriesFinder;
        this.episodeFinder = episodeFinder;
    }
    
    @Override
    public Brand fetch(String uri) {
        Matcher brandUriMatcher = brandUriPattern.matcher(uri);
        brandUriMatcher.matches();
        String brandId = brandUriMatcher.group(1);
        
        Brand brand = null;
        for (ItunesRegion region : ItunesRegion.values()) {
            Maybe<Brand> regionBrand = getBrandAndItemsForCountry(brandId, region);
            if (regionBrand.hasValue()) {
                if (brand == null) {
                    brand = regionBrand.requireValue();
                }
                else {
                    mergeItems(brand, regionBrand.requireValue());
                }
            }
        }
        
        return brand;
    }
    
    private void mergeItems(Brand to, Brand from) {
//        for (Episode item : from.getContents()) {
//            Maybe<Item> existingItem = findExistingItem(to, item);
//            if (existingItem.hasValue()) {
//                addLocations(existingItem.requireValue(), item);
//            }
//            else {
//                to.addContents(item);
//            }
//        }
    }
    
    private void addLocations(Item to, Item from) {
        Version version = Iterables.getOnlyElement(to.getVersions());
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        for (Location location : getAllLocations(from)) {
            encoding.addAvailableAt(location);
        }
    }
    
    private Set<Location> getAllLocations(Item item) {
        Set<Location> allLocations = Sets.newHashSet();
        
        for (Version version : item.getVersions()) {
            for (Encoding encoding : version.getManifestedAs()) {
                allLocations.addAll(encoding.getAvailableAt());
            }
        }
        
        return allLocations;
    }
    
    
    private Maybe<Item> findExistingItem(Brand brand, Item targetItem) {
//        for (Item item : brand.getContents()) {
//            if (targetItem.getCurie().equals(item.getCurie())) {
//                return Maybe.just(item);
//            }
//        }
        return Maybe.nothing();
    }

    private Maybe<Brand> getBrandAndItemsForCountry(String brandId, ItunesRegion region) {
        Maybe<Brand> brand = findBrand(brandId, region);
        if (brand.hasValue()) {
            Map<Long, Maybe<Series>> series = seriesFinder.findSeries(brandId, region);
            List<Episode> episodes = episodeFinder.findEpisodes(brandId, region, series);
            
            setGenres(brand.requireValue().getGenres(), series.values(), episodes);
            //brand.requireValue().setContents(episodes);
        }
        return brand;
    }
    
    private void setGenres(Set<String> genres, Iterable<Maybe<Series>> series, Iterable<Episode> episodes) {
        for (Maybe<Series> serie : series) {
            if (serie.hasValue()) {
                serie.requireValue().setGenres(genres);
            }
        }
        for (Episode episode : episodes) {
            episode.setGenres(genres);
        }
    }
    
    private Maybe<Brand> findBrand(String brandId, ItunesRegion region) {
        final String brandSearchUri = ItunesAdapterHelper.LOOKUP_URL_BASE + region.getSearchArgument() + "&id=" + brandId;
        
        try {
            return client.get(SimpleHttpRequest.httpRequestFrom(brandSearchUri, new HttpResponseTransformer<Maybe<Brand>>() {

                @Override
                public Maybe<Brand> transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                    if(HttpStatusCode.OK.code() != prologue.statusCode()) {
                        throw new HttpException(prologue.statusCode() + " response for " + brandSearchUri, prologue);
                    }
                    return extractBrand(body);
                }

                private Maybe<Brand> extractBrand(InputStream contents) throws Exception {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readValue(contents, JsonNode.class);
                    JsonNode resultsNode = rootNode.path("results");
                    
                    if (resultsNode.size() > 0) {
                        JsonNode brandNode = resultsNode.iterator().next();
                        
                        if (isBrandNode(brandNode)) {
                            String brandUri = brandNode.path("artistLinkUrl").getTextValue();
                            long artistId = brandNode.path("artistId").getNumberValue().longValue();
                            Brand brand = new Brand(brandUri, helper.getCurie(artistId), Publisher.ITUNES);
                            brand.setTitle(brandNode.path("artistName").getTextValue());
                            
                            int primaryGenre = brandNode.path("primaryGenreId").getNumberValue().intValue();
                            Set<String> genres = genreMap.mapRecognised(ImmutableSet.of(helper.getGenreUri(primaryGenre)));
                            brand.setGenres(genres);
                            
                            return Maybe.just(brand);
                        }
                    }
                    return Maybe.nothing();
                }
            }));
            
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class).withUri(brandSearchUri));
        }
        
        return Maybe.nothing();
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
    
    @Override
    public boolean canFetch(String uri) {
        return brandUriPattern.matcher(uri).matches();
    }
}
