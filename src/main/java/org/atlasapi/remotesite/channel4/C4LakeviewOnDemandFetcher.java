package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.MapMaker;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

public class C4LakeviewOnDemandFetcher {

	private static final String MEDIA_PLAYER = "media:player";
	private static final String DC_RELATED_ENTRY_ID = "dc:relation.RelatedEntryId";
	private static final String FEED_PREFIX = "http://ios.channel4.com/pmlsd/";
	
	private static final Pattern RELATED_ENTRY_PATTERN = Pattern.compile("^tag:.*(channel4.com,2009:)(.*)");
	private static final Pattern BRAND_PATTERN = Pattern.compile("^http://www.channel4.com/programmes/(.*?)/episode-guide.*");
	
	private RemoteSiteClient<Feed> atomClient;
	private final AdapterLog log;
	private Map<String, Map<String, Location>> locations;
	
	public C4LakeviewOnDemandFetcher(RemoteSiteClient<Feed> atomClient, AdapterLog log) {
		this.atomClient = atomClient;
		this.log = log;
		this.locations = new MapMaker().expireAfterWrite(30, TimeUnit.MINUTES).makeComputingMap(new Function<String, Map<String, Location> >() {
			@Override
			public Map<String, Location> apply(String brandUri) {
				return getBrandLocations(brandUri);
			}
		});
		
	}
	
	public Location lakeviewLocationFor(Item item) {
		Matcher matcher = BRAND_PATTERN.matcher(item.getCanonicalUri());
		if(matcher.matches()) {
			String brandName = matcher.group(1);
			return locations.get(brandName).get(item.getCanonicalUri());
		}
		return null;
	}
	
	private Map<String, Location> getBrandLocations(String brandName) {
		try {
			String uri = String.format("http://ios.channel4.com/pmlsd/%s/4od.atom", brandName);
			return extractLocations(atomClient.get(uri));
		} catch (HttpStatusCodeException e) {
			if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
				return ImmutableMap.<String, Location> builder().build();
			}
			else {
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
		for(Entry entry : entries) {
			Map<String, String> lookup = C4AtomApi.foreignElementLookup(entry);
			Matcher matcher = RELATED_ENTRY_PATTERN.matcher(lookup.get(DC_RELATED_ENTRY_ID));
			
			String episodeUri = null;
			if(matcher.matches()) {
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
			}
			else {
				log.record(AdapterLogEntry.errorEntry().withDescription("No media group for " + entry.getId()));
			}
		}
		return locations.build();
	}
	
}
