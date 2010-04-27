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

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.jherd.remotesite.timing.TimedFetcher;

import com.sun.syndication.feed.opml.Opml;

/**
 * Spike of an adapter to process an opml file, fetch all of the referenced feeds,
 * and build one representation merging in all the podcasts.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OpmlAdapter extends TimedFetcher<Representation> implements SiteSpecificRepresentationAdapter {

	private final RemoteSiteClient<Opml> opmlClient;
	private final BeanGraphExtractor<OpmlSource> graphExtractor;
	
	public OpmlAdapter(Fetcher<Representation> delegateFetcher) {
		this(new OpmlFeedClient(), new OpmlGraphExtractor(delegateFetcher));
	}

	OpmlAdapter(RemoteSiteClient<Opml> opmlFeedClient, BeanGraphExtractor<OpmlSource> graphExtractor) {
		this.opmlClient = opmlFeedClient;
		this.graphExtractor = graphExtractor;
	}

	@Override
	protected Representation fetchInternal(String uri, RequestTimer timer) {
		try {
			Opml feed = opmlClient.get(uri);
			return graphExtractor.extractFrom(new OpmlSource(feed, uri, timer));
		} catch (Exception e) {
			throw new FetchException("Problem fetching uri: " + uri, e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.endsWith("opml.xml") || uri.endsWith(".opml");
	}

}
