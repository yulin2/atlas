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

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.BeanGraphFactory;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link BeanGraphExtractor} that processes the entries of an RSS/Atom feed, and 
 * constructs a {@link Representation}, which may later be handled
 * by the {@link BeanGraphFactory}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class GenericPodcastGraphExtractor extends PodcastGraphExtractor implements BeanGraphExtractor<SyndicationSource> {
	
	private final IdGeneratorFactory idGeneratorFactory;

	public GenericPodcastGraphExtractor(IdGeneratorFactory idGeneratorFactory) {
		this.idGeneratorFactory = idGeneratorFactory;
	}

	public Representation extractFrom(SyndicationSource source) {
	
		Representation representation = new Representation();
	
		SyndFeed feed = source.getFeed();
		
		List<SyndEntry> entries = entriesFrom(feed);
		
		IdGenerator idGenerator = idGeneratorFactory.create();
		
		representation.addUri(source.getUri());
		representation.addType(source.getUri(), collectionType());
		
		Set<String> episodes = Sets.newHashSet();
		
		for (SyndEntry entry : entries) {
			
			String episodeId = addAndReturnUriOf(representation, idGenerator, entry);
			representation.addType(episodeId, Item.class);

			String versionId = idGenerator.getNextId();
			String encodingId = idGenerator.getNextId();
			String locationId = idGenerator.getNextId();
			
			episodes.add(episodeId);
			
			representation.addType(versionId, Version.class);
			representation.addAnonymous(versionId);
			
			representation.addValues(episodeId, extractEpisodePropertyValuesFrom(entry, versionId, source.getUri()));
			
			representation.addValues(versionId, extractVersionPropertyValuesFrom(encodingId));
			
			
			representation.addType(encodingId, Encoding.class);
			representation.addAnonymous(encodingId);
			representation.addValues(encodingId, extractEncodingPropertyValuesFrom(locationId, enclosuresFrom(entry)));
			
			representation.addAnonymous(locationId);
			representation.addType(locationId, Location.class);
			representation.addValues(locationId, extractLocationPropertyValuesFrom(locationUriFrom(entry)));
		}
		
		representation.addValues(source.getUri(), extractCollectionPropertyValuesFrom(episodes, feed, source.getUri()));
		
		return representation;
	}

	protected String addAndReturnUriOf(Representation representation, IdGenerator idGenerator, SyndEntry entry) {
		String uri = idGenerator.getNextId();
		representation.addAnonymous(uri);
		return uri;
	}

	protected Class<?> collectionType() {
		return Playlist.class;
	}

	private String locationUriFrom(SyndEntry entry) {
		String locationUri = "";
		List<SyndEnclosure> enclosures = enclosuresFrom(entry);
		if (enclosures != null && !enclosures.isEmpty()) {
			locationUri = enclosures.get(0).getUrl();
		}
		
		if (locationUri == null || "".equals(locationUri)) {
			locationUri = entry.getLink();
		}
		return locationUri;
	}

	protected MutablePropertyValues extractCollectionPropertyValuesFrom(Set<String> episodes, SyndFeed feed, String feedUri) {
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", feed.getTitle());
		mpvs.addPropertyValue("description", feed.getDescription());
		mpvs.addPropertyValue("items", episodes);
		mpvs.addPropertyValue("publisher", "bbc.co.uk");
		return mpvs;
	}

}
