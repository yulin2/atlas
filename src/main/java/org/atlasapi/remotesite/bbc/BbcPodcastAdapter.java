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
//package org.atlasapi.remotesite.bbc;
//
//import org.atlasapi.media.content.Container;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.persistence.system.RemoteSiteClient;
//import org.atlasapi.remotesite.ContentExtractor;
//import org.atlasapi.remotesite.SiteSpecificAdapter;
//import org.atlasapi.remotesite.synd.SyndicationAdapter;
//import org.atlasapi.remotesite.synd.SyndicationFeedClient;
//import org.atlasapi.remotesite.synd.SyndicationSource;
//
//import com.sun.syndication.feed.synd.SyndFeed;
//
///**
// * {@link SiteSpecificAdapter} for querying data about BBC podcasts.
// * 
// * @author Robert Chatley (robert@metabroadcast.com)
// */
//public class BbcPodcastAdapter extends SyndicationAdapter<Container<Item>> implements SiteSpecificAdapter<Container<Item>> {
//
//	public BbcPodcastAdapter() {
//		this(new SyndicationFeedClient(), new BbcPodcastGraphExtractor());
//	}
//	
//	protected BbcPodcastAdapter(RemoteSiteClient<SyndFeed> feedClient, ContentExtractor<SyndicationSource, Container<Item>> propertyExtractor) {
//		super(feedClient, propertyExtractor);
//	}
//	
//	public boolean canFetch(String uri) {
//		return uri.startsWith("http://downloads.bbc.co.uk") && uri.endsWith("rss.xml"); 
//	}
//}
