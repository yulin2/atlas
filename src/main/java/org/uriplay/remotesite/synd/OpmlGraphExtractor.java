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
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.bbc.BbcPodcastGenreMap;
import org.uriplay.remotesite.bbc.Policy;

import com.google.common.collect.Sets;
import com.sun.syndication.feed.opml.Outline;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OpmlGraphExtractor implements BeanGraphExtractor<OpmlSource> {

	private final Fetcher<Representation> delegateFetcher;
	private final BbcPodcastGenreMap genreMap = new BbcPodcastGenreMap();

	public OpmlGraphExtractor(Fetcher<Representation> delegateFetcher) {
		this.delegateFetcher = delegateFetcher;
	}

	public Representation extractFrom(OpmlSource source) {
		Representation representation = new Representation();
		Set<String> containedFeeds = Sets.newHashSet();
		
		List<Outline> outlines = typedList(source.getFeed().getOutlines());
		fetchFeedsFor(outlines, representation, containedFeeds, source.getTimer());
		
		addOuterPlaylist(source, containedFeeds, representation);
		
		return representation;
	}

	private void addOuterPlaylist(OpmlSource source, Set<String> containedFeeds, Representation representation) {
		representation.addUri(source.getUri());
		representation.addType(source.getUri(), Playlist.class);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("playlists", Sets.newHashSet(containedFeeds));
		mpvs.addPropertyValue("title", source.getTitle());
		mpvs.addPropertyValue("description", source.getTitle());
		representation.addValues(source.getUri(), mpvs);
	}
	
	private void fetchFeedsFor(List<Outline> outlines, Representation representation, Set<String> containedFeeds, RequestTimer timer) {
		if (outlines != null) {
			for (Outline outline : outlines) {
				fetchFeedsFor(outline, representation, containedFeeds, timer);
			}
		}
	}

	private void fetchFeedsFor(Outline outline, Representation representation, Set<String> containedFeeds, RequestTimer timer) {
		List<Outline> children = typedList(outline.getChildren());
		fetchFeedsFor(children, representation, containedFeeds, timer);
		
		if (outline.getXmlUrl() != null) {
			fetchFeed(outline.getXmlUrl(), outline, representation, containedFeeds, timer);
		} else if (outline.getHtmlUrl() != null) {
			fetchFeed(outline.getHtmlUrl(), outline, representation, containedFeeds, timer);
		} else if (outline.getUrl() != null) {
			fetchFeed(outline.getUrl(), outline, representation, containedFeeds, timer);
		}
	}

	private void fetchFeed(String feedUrl, Outline outline, Representation representation, Set<String> containedFeeds, RequestTimer timer) {
		
		String genres = outline.getAttributeValue("bbcgenres");
		
		Representation feedRepresentation;
		
		try {
			timer.nest();
			 feedRepresentation = delegateFetcher.fetch(feedUrl, timer);
		} catch (FetchException fe) {
			// carry on and try the next feed
			return;
		} finally {
			timer.unnest();
		}
		
		if (!StringUtils.isEmpty(genres)) {
			MutablePropertyValues mpvs = new MutablePropertyValues();
			mpvs.addPropertyValue("genres", map(genres));
			feedRepresentation.addValues(feedUrl, mpvs);
		}
		
		addPublishedDurationFor(outline, feedRepresentation);
		
		addRestrictionsFor(outline, feedRepresentation);
		
		representation.mergeIn(feedRepresentation);
		containedFeeds.add(feedUrl);
	}

	private void addRestrictionsFor(Outline outline, Representation feedRepresentation) {
		
		// foreach location 'restrictedBy' = 'allow == 'uk' ? 
		//      http://open.bbc.co.uk/rad/uriplay/policy/7days-uk-only : http://open.bbc.co.uk/rad/uriplay/policy/7days
		
		Set<Entry<String, Class<?>>> types = feedRepresentation.getTypes().entrySet();
		
		String allow = outline.getAttributeValue("allow");
		
		for (Entry<String, Class<?>> entry : types) {
			if (entry.getValue().equals(Location.class) && entry.getKey().contains("bbc.co.uk")) {
				MutablePropertyValues mpvs = new MutablePropertyValues();
				if ("uk".equals(allow)) {
					mpvs.addPropertyValue("restrictedBy", Policy.SEVEN_DAYS_UK_ONLY);
				} else {
					mpvs.addPropertyValue("restrictedBy", Policy.SEVEN_DAYS);
				}
				feedRepresentation.addValues(entry.getKey(), mpvs);
			}
		}
	}

	private void addPublishedDurationFor(Outline outline, Representation feedRepresentation) {
		Set<Entry<String, Class<?>>> types = feedRepresentation.getTypes().entrySet();

		// foreach version 'publishedDuration' = typicalDurationMins x 60 for secs

		String typicalDurationAttr = outline.getAttributeValue("typicalDurationMins");
	
		if (typicalDurationAttr != null) {
			Integer typicalDuration = Integer.parseInt(typicalDurationAttr);
			
			for (Entry<String, Class<?>> entry : types) {
				if (entry.getValue().equals(Version.class)) {
					MutablePropertyValues mpvs = new MutablePropertyValues();
					mpvs.addPropertyValue("publishedDuration", new Integer(typicalDuration * 60));
					feedRepresentation.addValues(entry.getKey(), mpvs);
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

	public Representation extractFrom(OpmlSource source, DescriptionMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

}
