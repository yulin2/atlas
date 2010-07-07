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

import org.uriplay.media.entity.Content;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.SiteSpecificAdapter;

import com.sun.syndication.feed.opml.Opml;

/**
 * Spike of an adapter to process an opml file, fetch all of the referenced feeds,
 * and build one representation merging in all the podcasts.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OpmlAdapter implements SiteSpecificAdapter<Content> {

	private final RemoteSiteClient<Opml> opmlClient;
	private final ContentExtractor<OpmlSource, Playlist> graphExtractor;
	
	public OpmlAdapter(Fetcher<Content> delegateFetcher) {
		this(new OpmlFeedClient(), new OpmlGraphExtractor(delegateFetcher));
	}

	OpmlAdapter(RemoteSiteClient<Opml> opmlFeedClient, ContentExtractor<OpmlSource, Playlist> graphExtractor) {
		this.opmlClient = opmlFeedClient;
		this.graphExtractor = graphExtractor;
	}

	@Override
	public Playlist fetch(String uri) {
		try {
			Opml feed = opmlClient.get(uri);
			return graphExtractor.extract(new OpmlSource(feed, uri));
		} catch (Exception e) {
			throw new FetchException("Problem fetching uri: " + uri, e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.endsWith("opml.xml") || uri.endsWith(".opml");
	}

}
