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

import org.uriplay.remotesite.BaseSource;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Object that wraps data fetched from a feed/podcast
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SyndicationSource extends BaseSource {

	private final SyndFeed feed;

	public SyndicationSource(SyndFeed feed, String uri) {
		super(uri);
		this.feed = feed;
	}

	public SyndFeed getFeed() {
		return feed;
	}
}
