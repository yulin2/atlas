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
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link SiteSpecificRepresentationAdapter} for querying data about BBC podcasts.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class GenericPodcastAdapter extends SyndicationAdapter implements SiteSpecificRepresentationAdapter {

	public GenericPodcastAdapter(IdGeneratorFactory idGeneratorFactory) {
		this(new SyndicationFeedClient(), new GenericPodcastGraphExtractor(idGeneratorFactory));
	}
	
	protected GenericPodcastAdapter(RemoteSiteClient<SyndFeed> feedClient, BeanGraphExtractor<SyndicationSource> propertyExtractor) {
		super(feedClient, propertyExtractor);
	}
	
	public boolean canFetch(String uri) {
		return true; // we use this adapter as the default, so match any url.
	}

}
