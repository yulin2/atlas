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

import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.SiteSpecificAdapter;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Base for {@link SiteSpecificAdapter}s dealing with syndicated feeds
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public abstract class SyndicationAdapter<T> implements Fetcher<T> {

	protected final RemoteSiteClient<SyndFeed> feedClient;
	protected final ContentExtractor<SyndicationSource, T> contentExtractor;

	public SyndicationAdapter(RemoteSiteClient<SyndFeed> feedClient, ContentExtractor<SyndicationSource, T> propertyExtractor) {
		this.feedClient = feedClient;
		this.contentExtractor = propertyExtractor;
	}

	@Override
	public T fetch(String uri) {
		try {
			SyndFeed feed = feedClient.get(uri);
			return contentExtractor.extract(new SyndicationSource(feed, uri));
		} catch (Exception e) {
			throw new FetchException("Failed to fetch: " + uri, e);
		}
	}
}
