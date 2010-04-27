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

package org.uriplay.remotesite.twitter;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class TwitterFeedGraphExtractor implements BeanGraphExtractor<SyndicationSource> {

	private static final String URL_REGEX = "(https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?)";
	
	private final Fetcher<Representation> fetcher;

	public TwitterFeedGraphExtractor(Fetcher<Representation> fetcher) {
		this.fetcher = fetcher;
	}

	public Representation extractFrom(SyndicationSource source) {
		
		Representation representation = new Representation();
		
		List<SyndEntry> entries = typedList(source.getFeed().getEntries());
		if (entries != null) {
			for (SyndEntry entry : entries) {
				fetchLinksFrom(entry.getTitle(), representation, source.getTimer());
			}
		}
	
		addOuterPlaylist(source, representation);
		
		return representation;
	}
	
	private void addOuterPlaylist(SyndicationSource source, Representation representation) {
		
		Set<String> items = Sets.newHashSet();
		Set<String> playlists = Sets.newHashSet();
		
		Set<Entry<String, Class<?>>> types = representation.getTypes().entrySet();
		for (Entry<String, Class<?>> typeEntry : types) {
			if (Item.class.isAssignableFrom(typeEntry.getValue())) {
				items.add(typeEntry.getKey());
			} else if (Playlist.class.isAssignableFrom(typeEntry.getValue())) {
				playlists.add(typeEntry.getKey());
			}
		}
		
		representation.addUri(source.getUri());
		representation.addType(source.getUri(), Playlist.class);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", "Twitter search: " + searchTermFrom(source.getUri()));
		mpvs.addPropertyValue("description", "Items found in twitter search: " + source.getUri());
		mpvs.addPropertyValue("items", Sets.newHashSet(items));
		mpvs.addPropertyValue("playlists", Sets.newHashSet(playlists));
		representation.addValues(source.getUri(), mpvs);
	}
	
	private String searchTermFrom(String uri) {
		return uri.substring(uri.lastIndexOf("q=") + 2);
	}

	private void fetchLinksFrom(String tweet, Representation representation, RequestTimer timer) {
		List<String> urls = linksIn(tweet);
		for (String url : urls) {
			try {
				timer.nest();
				Representation fetchedRepr = fetcher.fetch(url, timer);
				
				if (fetchedRepr != null) { // should throw exception rather than returning null from fetcher
					representation.mergeIn(fetchedRepr);
				}
			} catch (FetchException fe) {
				// carry on, try the next link
			} finally {
				timer.unnest();
			}
		}
	}

	private List<String> linksIn(String tweet) {
		Pattern regex = Pattern.compile(URL_REGEX);
		Matcher matcher = regex.matcher(tweet);
		List<String> links = Lists.newArrayList();
		while (matcher.find()) {
			links.add(matcher.group(1));
		} 
		return links;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> typedList(List<?> list) {
		return (List<T>)list;
	}

	public Representation extractFrom(SyndicationSource source, DescriptionMode mode) {
		// TODO Auto-generated method stub
		return null;
	}
}
