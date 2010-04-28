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

package org.uriplay.remotesite.channel4;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Broadcast;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.ContentExtractor;

import com.google.common.base.Splitter;
import com.google.soy.common.collect.Lists;
import com.google.soy.common.collect.Maps;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class C4BrandExtractor implements ContentExtractor<SyndFeed, Brand> {

	private static final String DC_GUIDANCE = "dc:relation.Guidance";
	private static final String DC_TERMS_AVAILABLE = "dcterms:available";
	private static final String DC_TX_DATE = "dc:date.TXDate";
	private static final String DC_TX_CHANNEL = "dc:relation.TXChannel";
	private static final String EPISODE_TITLE_TEMPLATE = "Series %s Episode %s";
	private static final String DC_EPISODE_NUMBER = "dc:relation.EpisodeNumber";
	private static final String DC_SERIES_NUMBER = "dc:relation.SeriesNumber";

	private static final Namespace NS_MEDIA_RSS = Namespace.getNamespace("http://search.yahoo.com/mrss/");
	
	private static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(\\d{4}-\\d{2}-\\d{2}); end=(\\d{4}-\\d{2}-\\d{2}); scheme=W3C-DTF");
	private static final String C4_PUBLISHER = "channel4.com";

	private static Map<String, String> CHANNEL_LOOKUP = channelLookup(); 
	
	private static Map<String, String> channelLookup() {
		Map<String, String> channelLookup = Maps.newHashMap();
		channelLookup.put("C4", "http://www.channel4.com");
		channelLookup.put("M4", "http://www.channel4.com/more4");
		channelLookup.put("E4", "http://www.e4.com");
		return channelLookup;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Brand extract(SyndFeed source) {
		String brandUri = source.getLink();

		Brand brand = new Brand();
		brand.setCanonicalUri(brandUri);
		brand.setTitle(title(source));
		brand.setCurie(PerPublisherCurieExpander.CurieAlgorithm.C4.compact(brandUri));
		brand.setPublisher(C4_PUBLISHER);

		for (SyndEntry entry : (List<SyndEntry>) source.getEntries()) {
			Map<String, String> lookup = foreignElementLookup(entry);
			
			Integer seriesNumber = readAsNumber(lookup, DC_SERIES_NUMBER);
			Integer episodeNumber = readAsNumber(lookup, DC_EPISODE_NUMBER);
			
			Episode episode = new Episode();
			String itemUri = entry.getLink();
			episode.setCanonicalUri(itemUri);
			episode.setCurie(PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri));

			if (episodeNumber != null && seriesNumber != null) {
				episode.setTitle(String.format(EPISODE_TITLE_TEMPLATE , seriesNumber, episodeNumber));
			}
			
			episode.setPublisher(C4_PUBLISHER);
			episode.setEpisodeNumber(episodeNumber);
			episode.setSeriesNumber(seriesNumber);
			
			Element mediaGroup = mediaGroup(entry);
			
			if (mediaGroup != null) {
				Element thumbnail = mediaGroup.getChild("thumbnail", NS_MEDIA_RSS);
				if (thumbnail != null) {
					Attribute thumbnailUri = thumbnail.getAttribute("url");
					episode.setThumbnail(thumbnailUri.getValue());
					episode.setImage(thumbnailUri.getValue().replace("200x113", "625x352"));
				}
			}
			
			episode.setIsLongForm(true);
			
			episode.setDescription(description(entry));
			
			episode.addVersion(version(itemUri, lookup));
			brand.addItem(episode);
		}
		return brand;
	}

	private Integer readAsNumber(Map<String, String> lookup, String key) {
		String value = lookup.get(key);
		if (value == null) {
			return null;
		}
		return Integer.valueOf(value);
	}

	private String description(SyndEntry entry) {
		SyndContent description = entry.getDescription();
		if (description == null) {
			return null;
		}
		return description.getValue();
	}
	
	private Location location(String uri, Map<String, String> lookup) {
		Location location = new Location();
		location.setUri(uri);
		location.setTransportType(TransportType.HTMLEMBED.toString().toLowerCase());
		
		// The feed only contains available content
		location.setAvailable(true);
		
		String availability = lookup.get(DC_TERMS_AVAILABLE);
		
		if (availability != null) {
			Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(availability);
			if (matcher.matches()) {
				location.setAvailabilityStart(new DateTime(matcher.group(1)));
				location.setAvailabilityEnd(new LocalDate(matcher.group(2)).plusDays(1).toDateTimeAtStartOfDay());
			}
		}
		return location;
	}

	private Version version(String uri, Map<String, String> lookup) {
		Version version = new Version();
		version.setDuration(durationFrom(lookup));
				
		String guidance = lookup.get(DC_GUIDANCE);
		if (guidance != null) {
			version.setRating("http://uriplay.org/ratings/simple/adult");
			version.setRatingText(guidance);
		} else {
			version.setRating("http://uriplay.org/ratings/simple/nonadult");
		} 
		
		String txChannel = CHANNEL_LOOKUP.get(lookup.get(DC_TX_CHANNEL));
		String txDate = lookup.get(DC_TX_DATE);
		if (txChannel != null) {
			Broadcast broadcast = new Broadcast();
			broadcast.setBroadcastOn(txChannel);
			broadcast.setTransmissionTime(new DateTime(txDate));
			version.addBroadcast(broadcast);
		}
		
		
		Encoding encoding = new Encoding();
		encoding.addAvailableAt(location(uri, lookup));
		version.addManifestedAs(encoding);
		return version;
	}


	private Integer durationFrom(Map<String, String> lookup) {
		String durationString = lookup.get("dc:relation.Duration");
		if (durationString == null) {
			return null;
		}
		List<String> parts = Lists.newArrayList(Splitter.on(":").split(durationString));
		int duration = 0;
		for (String part : parts) {
			duration = (duration * 60) + Integer.valueOf(part);
		}
		return duration;
	}

	@SuppressWarnings("unchecked")
	private Element mediaGroup(SyndEntry syndEntry) {
		for (Element element : (List<Element>) syndEntry.getForeignMarkup()) {
			if (NS_MEDIA_RSS.equals(element.getNamespace()) && "group".equals(element.getName())) {
				return element;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> foreignElementLookup(SyndEntry syndEntry) {
		Map<String, String> foreignElementLookup = Maps.newHashMap();
		for (Element element : (List<Element>) syndEntry.getForeignMarkup()) {
			foreignElementLookup.put(element.getNamespacePrefix() + ":" + element.getName(), element.getText());
		}
		return foreignElementLookup;
	}
	
	private String title(SyndFeed source) {
		return source.getTitle().replace(" on 4oD", "");
	}
}
