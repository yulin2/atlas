package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.jdom.Element;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

public class C4LakeviewOnDemandFetcher {

    private static final String API_ROOT = "http://api.channel4.com/pmlsd/";
    private static final String PLATFORM = "p06";
    private static final String DC_RELATED_ENTRY_ID = "dc:relation.RelatedEntryId";
    private static final Pattern RELATED_ENTRY_PATTERN = Pattern.compile("^tag:.*(channel4.com,2009:)(.*)");
    private static final Pattern BRAND_PATTERN = Pattern.compile("^http://www.channel4.com/programmes/(.*?)/episode-guide.*");
    private RemoteSiteClient<Feed> atomClient;
    private final AdapterLog log;
    private LoadingCache<String, Map<String, Location>> locations;
    private final String apiKey;

    /**
     * Caching lakeview ondemand fetcher
     *
     * @param atomClient
     * @param apiRoot	API root, e.g. http://www.channel4.com/pmlsd/ . Must
     * include trailing forwardslash.
     * @param log
     */
    public C4LakeviewOnDemandFetcher(RemoteSiteClient<Feed> atomClient, String apiKey, AdapterLog log) {
        this.atomClient = atomClient;
        this.apiKey = apiKey;
        this.log = log;
        this.locations = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<String, Map<String, Location>>() {

            @Override
            public Map<String, Location> load(String brandUri) {
                return getBrandLocations(brandUri);
            }
        });

    }

    public Location lakeviewLocationFor(Item item) {
        try {
            Matcher matcher = BRAND_PATTERN.matcher(item.getCanonicalUri());
            if (matcher.matches()) {
                String brandName = matcher.group(1);
                return locations.get(brandName).get(item.getCanonicalUri());
            }
            return null;
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private Map<String, Location> getBrandLocations(String brandName) {
        try {
            String uri = String.format("%s%s/4od.atom?platform=%s&apiKey=%s", API_ROOT, brandName, 
                    PLATFORM, apiKey);
            return extractLocations(atomClient.get(uri));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
                return ImmutableMap.<String, Location>builder().build();
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Location> extractLocations(Feed source) {
        List<Entry> entries = source.getEntries();
        Builder<String, Location> locations = ImmutableMap.builder();
        for (Entry entry : entries) {
            Map<String, String> lookup = C4AtomApi.foreignElementLookup(entry);
            Matcher matcher = RELATED_ENTRY_PATTERN.matcher(lookup.get(DC_RELATED_ENTRY_ID));

            String episodeUri = null;
            if (matcher.matches()) {
                episodeUri = String.format("http://www.channel4.com%s", matcher.group(2));
            }

            Element mediaGroup = C4AtomApi.mediaGroup(entry);
            Set<Country> availableCountries = null;
            if (mediaGroup != null) {
                Element restriction = mediaGroup.getChild("restriction", C4AtomApi.NS_MEDIA_RSS);
                if (restriction != null && restriction.getValue() != null) {
                    availableCountries = Countries.fromDelimtedList(restriction.getValue());
                }
                String uri = mediaGroup.getChild("player", C4AtomApi.NS_MEDIA_RSS).getAttributeValue("url");
                Location location = C4AtomApi.locationFrom(uri, null, lookup, availableCountries, null, Platform.XBOX);
                locations.put(episodeUri, location);
            } else {
                log.record(AdapterLogEntry.errorEntry().withDescription("No media group for " + entry.getId()));
            }
        }
        return locations.build();
    }
}
