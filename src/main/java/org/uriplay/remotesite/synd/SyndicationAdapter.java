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

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.jherd.remotesite.timing.TimedFetcher;
import org.uriplay.remotesite.ContentExtractor;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Base for {@link SiteSpecificAdapter}s dealing with syndicated feeds
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public abstract class SyndicationAdapter<T> extends TimedFetcher<T> {

	protected final RemoteSiteClient<SyndFeed> feedClient;
	protected final ContentExtractor<SyndicationSource, T> contentExtractor;

	public SyndicationAdapter(RemoteSiteClient<SyndFeed> feedClient, ContentExtractor<SyndicationSource, T> propertyExtractor) {
		this.feedClient = feedClient;
		this.contentExtractor = propertyExtractor;
	}

	@Override
	protected T fetchInternal(String uri, RequestTimer timer) {
		try {
			SyndFeed feed = feedClient.get(uri);
			return contentExtractor.extract(new SyndicationSource(feed, uri, timer));
		} catch (Exception e) {
			throw new FetchException("Failed to fetch: " + uri, e);
		}
	}
}
