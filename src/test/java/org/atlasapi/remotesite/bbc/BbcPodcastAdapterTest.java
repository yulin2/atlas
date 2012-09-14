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
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.instanceOf;
//
//import java.io.IOException;
//
//import org.atlasapi.media.content.Container;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.persistence.system.RemoteSiteClient;
//import org.atlasapi.remotesite.ContentExtractor;
//import org.atlasapi.remotesite.FetchException;
//import org.atlasapi.remotesite.synd.SyndicationSource;
//import org.jmock.Expectations;
//import org.jmock.integration.junit3.MockObjectTestCase;
//
//import com.sun.syndication.feed.synd.SyndFeed;
//
///**
// * Unit test for {@link BbcPodcastAdapter}.
// * @author Robert Chatley (robert@metabroadcast.com)
// */
//public class BbcPodcastAdapterTest extends MockObjectTestCase {
//
//	static final String DOCUMENT = "doc";
//	
//	RemoteSiteClient<SyndFeed> feedClient;
//	ContentExtractor<SyndicationSource, Container<Item>> propertyExtractor;
//	BbcPodcastAdapter adapter;
//	SyndFeed feed = null;
//	SyndicationSource podcastSource;
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		feedClient = mock(RemoteSiteClient.class);
//		propertyExtractor = mock(ContentExtractor.class);
//		adapter = new BbcPodcastAdapter(feedClient, propertyExtractor);
//		podcastSource = new SyndicationSource(feed, "http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml");
//	}
//	
//	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
//		
//		checking(new Expectations() {{
//			one(feedClient).get("http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml"); will(returnValue(feed));
//			one(propertyExtractor).extract(podcastSource);
//		}});
//		
//		adapter.fetch("http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml");
//	}
//	
//	public void testWrapsExceptionIfRemoteClientThrowsException() throws Exception {
//		
//		checking(new Expectations() {{
//			allowing(feedClient).get("http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml"); will(throwException(new IOException()));
//		}});
//		
//		try {
//			adapter.fetch("http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml");
//			
//			fail("Should have thrown FetchException.");
//		} catch (Exception e) {
//			assertThat(e, instanceOf(FetchException.class));
//		}
//	}
//	
//	public void testCanFetchResourcesForRssUris() throws Exception {
//		assertTrue(adapter.canFetch("http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml"));
//		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
//	}
//	
//}
