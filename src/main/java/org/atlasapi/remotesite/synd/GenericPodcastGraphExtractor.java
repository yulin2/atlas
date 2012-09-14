///* Copyright 2009 British Broadcasting Corporation
//   Copyright 2009 Meta Broadcast Ltd
//
//Licensed under the Apache License, Version 2.0 (the "License"); you
//may not use this file except in compliance with the License. You may
//obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//implied. See the License for the specific language governing
//permissions and limitations under the License. */
//
//package org.atlasapi.remotesite.synd;
//
//import java.util.List;
//
//import org.atlasapi.media.content.Container;
//import org.atlasapi.media.entity.Encoding;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.media.entity.Location;
//import org.atlasapi.media.entity.Version;
//import org.atlasapi.remotesite.ContentExtractor;
//
//import com.google.common.collect.Lists;
//import com.sun.syndication.feed.synd.SyndEnclosure;
//import com.sun.syndication.feed.synd.SyndEntry;
//import com.sun.syndication.feed.synd.SyndFeed;
//
///**
// * {@link BeanGraphExtractor} that processes the entries of an RSS/Atom feed, and 
// * constructs a {@link Representation}, which may later be handled
// * by the {@link BeanGraphFactory}.
// *
// * @author Robert Chatley (robert@metabroadcast.com)
// */
//public abstract class GenericPodcastGraphExtractor extends PodcastGraphExtractor implements ContentExtractor<SyndicationSource, Container<Item>> {
//
//	public Container<Item> extract(SyndicationSource source) {
//	
//		SyndFeed feed = source.getFeed();
//		
//		List<SyndEntry> entries = entriesFrom(feed);
//		
//		Container<Item> playlist =  collectionType();
//		playlist.setCanonicalUri(source.getUri());
//		setCollectionPropertyValuesFrom(playlist, feed, source.getUri());
//		
//		List<Item> items = Lists.newArrayList();
//		for (SyndEntry entry : entries) {
//			Location location =  locationFrom(locationUriFrom(entry));
//			
//			Encoding encoding = encodingFrom(enclosuresFrom(entry));
//			encoding.addAvailableAt(location);
//			
//			Version version = new Version();
//			version.addManifestedAs(encoding);
//			
//			Item item = itemFrom(entry, source.getUri());
//			item.addVersion(version);
//			items.add(item);
//
//		}
//		
//		playlist.setContents(items);
//		
//		playlist.setPublisher(publisher());
//		
//		return playlist;
//	}
//
//	protected Container<Item> collectionType() {
//		return new Container<Item>();
//	}
//
//	private String locationUriFrom(SyndEntry entry) {
//		String locationUri = "";
//		List<SyndEnclosure> enclosures = enclosuresFrom(entry);
//		if (enclosures != null && !enclosures.isEmpty()) {
//			locationUri = enclosures.get(0).getUrl();
//		}
//		
//		if (locationUri == null || "".equals(locationUri)) {
//			locationUri = entry.getLink();
//		}
//		return locationUri;
//	}
//
//	protected void setCollectionPropertyValuesFrom(Container<?> playlist, SyndFeed feed, String feedUri) {
//		playlist.setTitle(feed.getTitle());
//		playlist.setDescription(feed.getDescription());
//		playlist.setCanonicalUri(feedUri);
//	}
//
//}
