/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.synd;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.uriplay.media.entity.Countries;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Policy;
import org.uriplay.media.entity.Version;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.bbc.BbcPodcastGenreMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.opml.Outline;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OpmlGraphExtractor implements ContentExtractor<OpmlSource, Playlist> {

	private final Fetcher<Object> delegateFetcher;
	private final BbcPodcastGenreMap genreMap = new BbcPodcastGenreMap();

	public OpmlGraphExtractor(Fetcher<Object> delegateFetcher) {
		this.delegateFetcher = delegateFetcher;
	}

	public Playlist extract(OpmlSource source) {
		Playlist playlist = outerPlaylist(source);

		List<Outline> outlines = typedList(source.getFeed().getOutlines());
	
		playlist.setPlaylists(fetchFeedsFor(outlines));
		
		return playlist;
	}

	private Playlist outerPlaylist(OpmlSource source) {
		Playlist playlist = new Playlist();
		playlist.setCanonicalUri(source.getUri());
		playlist.setTitle(source.getTitle());
		playlist.setDescription(source.getTitle());
		return playlist;
	}
	
	private List<Playlist> fetchFeedsFor(List<Outline> outlines) {
		List<Playlist> playlists = Lists.newArrayList();
		if (outlines != null) {
			for (Outline outline : outlines) {
				playlists.addAll(fetchFeedsFor(outline));
			}
		}
		return playlists;
	}

	private List<Playlist> fetchFeedsFor(Outline outline) {
		List<Playlist> playlists = Lists.newArrayList();

		List<Outline> children = typedList(outline.getChildren());
		playlists.addAll(fetchFeedsFor(children));
		
		if (outline.getXmlUrl() != null) {
			playlists.addAll(fetchFeed(outline.getXmlUrl(), outline));
		} else if (outline.getHtmlUrl() != null) {
			playlists.addAll(fetchFeed(outline.getHtmlUrl(), outline));
		} else if (outline.getUrl() != null) {
			playlists.addAll(fetchFeed(outline.getUrl(), outline));
		}
		return playlists;
	}

	private List<Playlist> fetchFeed(String feedUrl, Outline outline) {
		
		String genres = outline.getAttributeValue("bbcgenres");
		
		Playlist feed;
		
		try {
			 feed = (Playlist) delegateFetcher.fetch(feedUrl);
		} catch (FetchException fe) {
			// carry on and try the next feed
			return Lists.newArrayList();
		} 
		
		if (!StringUtils.isEmpty(genres)) {
			Set<String> allGenres = Sets.newHashSet(feed.getGenres());
			allGenres.addAll(map(genres));
			feed.setGenres(allGenres);
		}
		
		addPublishedDurationFor(outline, feed);
		
		addRestrictionsFor(outline, feed);
		
		return Lists.newArrayList(feed);
	}

	private void addRestrictionsFor(Outline outline, Playlist feed) {
		
		String allow = outline.getAttributeValue("allow");
		
		for (Location location : locationsFrom(feed.getItems())) {
			if (location.getUri() != null && location.getUri().contains("bbc.co.uk")) {
				Policy policy = location.getPolicy();
				if (policy == null) {
					policy = new Policy();
					location.setPolicy(policy);
				}
				policy.setAvailabilityLength((int) TimeUnit.SECONDS.convert(7, TimeUnit.DAYS));
				if ("uk".equals(allow)) {
					policy.addAvailableCountry(Countries.GB);
				} 
			}
		}
	}

	private Iterable<Location> locationsFrom(Iterable<Item> items) {
		Set<Location> locations = Sets.newHashSet();
		for (Item item : items) {
			for (Version version : item.getVersions()) {
				for (Encoding encoding : version.getManifestedAs()) {
					for (Location location : encoding.getAvailableAt()) {
						locations.add(location);
					}
				}
			}
		}
		return locations;
	}

	private void addPublishedDurationFor(Outline outline, Playlist feed) {

		// foreach version 'publishedDuration' = typicalDurationMins x 60 for secs

		String typicalDurationAttr = outline.getAttributeValue("typicalDurationMins");
	
		if (typicalDurationAttr != null) {
			Integer typicalDuration = Integer.parseInt(typicalDurationAttr);
			for (Item item : feed.getItems()) {
				for (Version version : item.getVersions()) {
					version.setDuration(Duration.standardMinutes(typicalDuration));
				}
			}
		}
	}

	private Set<String> map(String pipeSeparatedGenreString) {

		Set<String> results = Sets.newHashSet();
		for (String genre : pipeSeparatedGenreString.split("\\|")) {
			String mappedGenre = genreMap.lookup(genre);
			if (mappedGenre != null) {
				results.add(mappedGenre);
			}
		}
		
		return results;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> typedList(List<?> list) {
		return (List<T>)list;
	}
}
