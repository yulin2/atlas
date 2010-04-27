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

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.remotesite.synd.SyndicationAdapter;
import org.uriplay.remotesite.synd.SyndicationFeedClient;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link SiteSpecificRepresentationAdapter} that retrieves info from twitter search atom feeds, and then
 * recursively processes any links found within the feed.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class TwitterAdapter extends SyndicationAdapter implements SiteSpecificRepresentationAdapter {
	
	public TwitterAdapter(Fetcher<Representation> delgateFetcher) {
		this(new SyndicationFeedClient(), new TwitterFeedGraphExtractor(delgateFetcher));
	}
	
	public TwitterAdapter(RemoteSiteClient<SyndFeed> feedClient, BeanGraphExtractor<SyndicationSource> propertyExtractor) {
		super(feedClient, propertyExtractor);
	}
	
	@Override
	protected Representation fetchInternal(String uri, RequestTimer timer) {
		try {
			SyndFeed feed = feedClient.get(atomFeed(uri));
			return propertyExtractor.extractFrom(new SyndicationSource(feed, uri, timer));
		} catch (Exception e) {
			throw new FetchException("Failed to fetch: " + uri, e);
		}
	}
	
	private String atomFeed(String uri) {
		uri = uri + "&filter=links&rpp=50";
		if (uri.contains("search.twitter.com/search?")) {
			return uri.replace("/search?" , "/search.atom?");
		}
		return uri;
	}

	public boolean canFetch(String uri) {
		return uri.contains("search.twitter.com/search") ;
	}
}
