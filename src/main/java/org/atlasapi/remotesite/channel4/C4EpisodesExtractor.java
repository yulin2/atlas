/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Country;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.ContentExtractor;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;

public class C4EpisodesExtractor implements ContentExtractor<Feed, List<Episode>> {

	private static final String DC_DURATION = "dc:relation.Duration";
	private static final String DC_GUIDANCE = "dc:relation.Guidance";
	private static final String DC_TERMS_AVAILABLE = "dcterms:available";
	private static final String DC_TX_DATE = "dc:date.TXDate";
	private static final String DC_TX_CHANNEL = "dc:relation.TXChannel";
	private static final String EPISODE_TITLE_TEMPLATE = "Series %s Episode %s";
	private static final String DC_EPISODE_NUMBER = "dc:relation.EpisodeNumber";
	private static final String DC_SERIES_NUMBER = "dc:relation.SeriesNumber";

	private static final Namespace NS_MEDIA_RSS = Namespace.getNamespace("http://search.yahoo.com/mrss/");
	
	private static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(\\d{4}-\\d{2}-\\d{2}); end=(\\d{4}-\\d{2}-\\d{2}); scheme=W3C-DTF");

	private static Map<String, String> CHANNEL_LOOKUP = channelLookup(); 
	
	private static Map<String, String> channelLookup() {
		Map<String, String> channelLookup = Maps.newHashMap();
		channelLookup.put("C4", "http://www.channel4.com");
		channelLookup.put("M4", "http://www.channel4.com/more4");
		channelLookup.put("E4", "http://www.e4.com");
		return channelLookup;
	}
	
	private boolean include4odInfo = false;
	private boolean inlcudeBroadcasts = false;
	
	public C4EpisodesExtractor includeOnDemands() {
		this.include4odInfo = true;
		return this;
	}
	
	public C4EpisodesExtractor includeBroadcasts() {
		this.inlcudeBroadcasts = true;
		return this;
	}
	
	private static final Pattern SERIES_AND_EPISODE_NUMBER = Pattern.compile("series-(\\d+)/episode-(\\d+)");
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Episode> extract(Feed source) {
		
		String feedTitle = Strings.nullToEmpty(source.getTitle());
		
		List<Episode> episodes = Lists.newArrayList();
		
		for (Entry entry : (List<Entry>) source.getEntries()) {
			Map<String, String> lookup = foreignElementLookup(entry);
			
			Integer seriesNumber = readAsNumber(lookup, DC_SERIES_NUMBER);
			Integer episodeNumber = readAsNumber(lookup, DC_EPISODE_NUMBER);
			
			String itemUri = canonicalUri(entry);
			
			if (itemUri == null) {
				// fall back to hacking the uri out of the feed
				itemUri = extarctUriFromLink(source, entry);
			}
			if (itemUri == null) {
				throw new IllegalStateException("Cannot extract canonical URI for entry with id " + entry.getId());
			}
			
			Episode episode = new Episode(itemUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri), Publisher.C4);

			String fourOdUri = fourOdUri(entry);
			if (fourOdUri != null) {
				episode.addAlias(fourOdUri);
			}
			
			episode.addAlias(entry.getId());

			episode.setTitle(title(entry));
			
			if ((Strings.isNullOrEmpty(episode.getTitle()) || feedTitle.startsWith(episode.getTitle())) && episodeNumber != null && seriesNumber != null) {
				episode.setTitle(String.format(EPISODE_TITLE_TEMPLATE , seriesNumber, episodeNumber));
			}
			
			episode.setEpisodeNumber(episodeNumber);
			episode.setSeriesNumber(seriesNumber);
			
			Element mediaGroup = mediaGroup(entry);
			
			Set<Country> availableCountries = null;
			
			if (mediaGroup != null) {
				Element thumbnail = mediaGroup.getChild("thumbnail", NS_MEDIA_RSS);
				if (thumbnail != null) {
					Attribute thumbnailUri = thumbnail.getAttribute("url");
					C4AtomApi.addImages(episode, thumbnailUri.getValue());
				}
				Element restriction = mediaGroup.getChild("restriction", NS_MEDIA_RSS);
				if (restriction != null && restriction.getValue() != null) {
					availableCountries = Countries.fromDelimtedList(restriction.getValue());
				}
			}
			
			episode.setIsLongForm(true);
			episode.setDescription(description(entry));
			
			Version version = version(fourOdUri(entry), lookup, availableCountries);
			if (version != null) {
				episode.addVersion(version);
			}
			episodes.add(episode);
		}
		return episodes;
	}

	@SuppressWarnings("unchecked")
	private String extarctUriFromLink(Feed source, Entry entry) {
		for (Link link : (List<Link>) entry.getOtherLinks()) {
			Matcher matcher = SERIES_AND_EPISODE_NUMBER.matcher(link.getHref());
			if (matcher.find()) {
				return C4AtomApi.episodeUri(C4AtomApi.webSafeNameFromAnyFeedId(source.getId()), Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));	
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String fourOdUri(Entry entry) {
		List<Link> links = entry.getAlternateLinks();
		
		for (Link link : links) {
			String href = link.getHref();
			if (href.contains("4od#")) {
				return href;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private String canonicalUri(Entry entry) {
		List<Link> links = entry.getAlternateLinks();
		
		for (Link link : links) {
			String href = link.getHref();
			if (C4AtomApi.isACanonicalEpisodeUri(href)) {
				return href;
			}
		}
		return null;
	}
	
	
	

	private Integer readAsNumber(Map<String, String> lookup, String key) {
		String value = lookup.get(key);
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		return Integer.valueOf(value);
	}

	private String description(Entry entry) {
		com.sun.syndication.feed.atom.Content description = entry.getSummary();
		if (description == null) {
			return null;
		}
		return description.getValue();
	}
	
	private String title(Entry entry) {
        com.sun.syndication.feed.atom.Content title = entry.getTitleEx();
        if (title == null) {
            return null;
        }
        return title.getValue();
    }
	
	private Location location(String uri, Map<String, String> lookup, Set<Country> availableCountries) {
		Location location = new Location();
		location.setUri(uri);
		location.setTransportType(TransportType.LINK);
		
		// The feed only contains available content
		location.setAvailable(true);
		
		String availability = lookup.get(DC_TERMS_AVAILABLE);
		
		if (availability != null) {
			Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(availability);
			if (matcher.matches()) {
				Policy policy = new Policy()
					.withAvailabilityStart(new DateTime(matcher.group(1)))
					.withAvailabilityEnd(new LocalDate(matcher.group(2)).plusDays(1).toDateTimeAtStartOfDay());
					
				if (availableCountries != null) {
					policy.setAvailableCountries(availableCountries);
				}
				location.setPolicy(policy);
			}
		}
		return location;
	}


	private Version version(String uri, Map<String, String> lookup, Set<Country> availableCountries) {
		Version version = new Version();
		Duration duration = durationFrom(lookup);
		
		if (duration != null) {
			version.setDuration(duration);
		}
		
		String guidance = lookup.get(DC_GUIDANCE);
		if (guidance != null) {
			version.setRating("http://ref.atlasapi.org/ratings/simple/adult");
			version.setRatingText(guidance);
		} else {
			version.setRating("http://ref.atlasapi.org/ratings/simple/nonadult");
		} 
		
		if (inlcudeBroadcasts) {
			String txChannel = CHANNEL_LOOKUP.get(lookup.get(DC_TX_CHANNEL));
			String txDate = lookup.get(DC_TX_DATE);
			if (txChannel != null) {
				DateTime txStart = new DateTime(txDate);
				Broadcast broadcast = new Broadcast(txChannel, txStart, duration);
				version.addBroadcast(broadcast);
			}
		}
		
		if (include4odInfo) {
			Encoding encoding = new Encoding();
			encoding.addAvailableAt(location(uri, lookup, availableCountries));
			version.addManifestedAs(encoding);
		}
		
		
		return version.getBroadcasts().isEmpty() && version.getManifestedAs().isEmpty() ? null : version;
	}


	private Duration durationFrom(Map<String, String> lookup) {
		String durationString = lookup.get(DC_DURATION);
		if (durationString == null) {
			return null;
		}
		List<String> parts = Lists.newArrayList(Splitter.on(":").split(durationString));
		int duration = 0;
		for (String part : parts) {
			duration = (duration * 60) + Integer.valueOf(part);
		}
		return Duration.standardSeconds(duration);
	}

	@SuppressWarnings("unchecked")
	private Element mediaGroup(Entry syndEntry) {
		for (Element element : (List<Element>) syndEntry.getForeignMarkup()) {
			if (NS_MEDIA_RSS.equals(element.getNamespace()) && "group".equals(element.getName())) {
				return element;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> foreignElementLookup(Entry entry) {
		return foreignElementLookup((Iterable<Element>) entry.getForeignMarkup());
	}

	private Map<String, String> foreignElementLookup(Iterable<Element> foreignMarkup) {
		Map<String, String> foreignElementLookup = Maps.newHashMap();
		for (Element element : foreignMarkup) {
			foreignElementLookup.put(element.getNamespacePrefix() + ":" + element.getName(), element.getText());
		}
		return foreignElementLookup;
	}
	
}
