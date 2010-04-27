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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jherd.beans.Representation;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Unit test for {@link TwitterFeedGraphExtractor}
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class TwitterFeedGraphExtractorTest extends MockObjectTestCase {

	private static final String SEARCH_TERM = "imdb.com";
	static final String TWITTER_FEED_URI = "http://search.twitter.com/search.atom?q=" +	SEARCH_TERM;
	static final String LINK1_URI = "http://www.imdb.com/title/tt0071411/";
	static final String LINK2_URI = "http://www.youtube.com/watch?v=PpUbNJIJx9I";
	static final String LINK3_URI = "http://www.bbc.co.uk/podcasts/bh.rss";
	
	Fetcher fetcher = mock(Fetcher.class);
	RequestTimer timer = mock(RequestTimer.class);
	
	TwitterFeedGraphExtractor extractor = new TwitterFeedGraphExtractor(fetcher);
	SyndFeed feed;
	SyndicationSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feed = createFeed("Entry talking about " + LINK1_URI, "Tweet mentioning " + LINK2_URI + " which is great.", "Podcast: " + LINK3_URI);
		source = new SyndicationSource(feed, TWITTER_FEED_URI, timer);
	}

	private SyndFeed createFeed(String title1, String title2, String title3) {
		SyndFeed feed = new SyndFeedImpl();
		SyndEntry entry1 = new SyndEntryImpl();
		entry1.setTitle(title1);
		SyndEntry entry2 = new SyndEntryImpl();
		entry2.setTitle(title2);
		SyndEntry entry3 = new SyndEntryImpl();
		entry3.setTitle(title3);
		feed.setEntries(Lists.newArrayList(entry1, entry2, entry3));
		return feed;
	}
	
	public void testFetchesAllMentionedLinksAndMergesTheirRepresentations() throws Exception {
		
		final Representation representation1 = new Representation();
		representation1.addUri(LINK1_URI);
		representation1.addType(LINK1_URI, Episode.class);
		final Representation representation2 = new Representation();
		representation1.addUri(LINK2_URI);
		representation2.addType(LINK2_URI, Episode.class);
		final Representation representation3 = new Representation();
		representation1.addUri(LINK3_URI);
		representation3.addType(LINK3_URI, Playlist.class);
		
		checking(new Expectations() {{
			one(fetcher).fetch(LINK1_URI, timer); will(returnValue(representation1));
			one(fetcher).fetch(LINK2_URI, timer); will(returnValue(representation2));
			one(fetcher).fetch(LINK3_URI, timer); will(returnValue(representation3));
			exactly(3).of(timer).nest();
			exactly(3).of(timer).unnest();
		}});
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Episode.class, representation.getType(LINK1_URI));
		assertEquals(Episode.class, representation.getType(LINK2_URI));
		assertEquals(Playlist.class, representation.getType(LINK3_URI));
		assertEquals(Playlist.class, representation.getType(TWITTER_FEED_URI));
		assertThat(representation, hasPropertyValue(TWITTER_FEED_URI, "title", "Twitter search: " + SEARCH_TERM));
		assertThat(representation, hasPropertyValue(TWITTER_FEED_URI, "description", "Items found in twitter search: " + TWITTER_FEED_URI));
		assertThat(representation, hasPropertyValue(TWITTER_FEED_URI, "items", Sets.newHashSet(LINK1_URI, LINK2_URI)));
		assertThat(representation, hasPropertyValue(TWITTER_FEED_URI, "playlists", Sets.newHashSet(LINK3_URI)));
	}

}
