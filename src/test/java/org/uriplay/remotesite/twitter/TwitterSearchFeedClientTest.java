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
import junit.framework.TestCase;

import org.jherd.util.testing.EnvironmentTest;

/**
 * Unit test for {@link TwitterSearchFeedClient}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@EnvironmentTest
public class TwitterSearchFeedClientTest extends TestCase {

	public void testCanReadRssFeed() throws Exception {

		// suppress, unreliable response from Twitter - 26/08/09 - rchatley
		
//		String feedUrl = "http://search.twitter.com/search.atom?q=imdb";
//
//		SyndFeed feed = new SyndicationFeedClient().get(feedUrl);
//
//		assertThat(feed.getTitle(), is("imdb - Twitter Search"));
//
//		List<SyndEntry> entries = entriesFrom(feed);
//		assertThat(entries.size(), is(greaterThan(10)));
	}

//	@SuppressWarnings("unchecked")
//	private List<SyndEntry> entriesFrom(SyndFeed feed) {
//		return feed.getEntries();
//	}
}
