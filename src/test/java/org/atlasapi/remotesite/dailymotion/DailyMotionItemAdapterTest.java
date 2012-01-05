/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.dailymotion;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.html.HtmlDescriptionSource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link C4BrandAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
@RunWith(JMock.class)
public class DailyMotionItemAdapterTest extends TestCase {

    private final Mockery context = new Mockery();
    
	String uri = "http://www.dailymotion.com/video/x9l8e7_protesting-irans-election_news";
	
	RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	ContentExtractor<HtmlDescriptionSource, Item> propertyExtractor;
	DailyMotionItemAdapter adapter;
	
	Item item = new Item();
	HtmlDescriptionOfItem htmlItem = new HtmlDescriptionOfItem();
	
	HtmlDescriptionSource source = new HtmlDescriptionSource(htmlItem, uri);
	
	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		itemClient = context.mock(RemoteSiteClient.class);
		propertyExtractor = context.mock(ContentExtractor.class);
		adapter = new DailyMotionItemAdapter(itemClient, propertyExtractor);
	}

    @Test
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		context.checking(new Expectations() {{
			one(itemClient).get(uri); will(returnValue(htmlItem));
			one(propertyExtractor).extract(source); will(returnValue(item));
		}});
		
		assertEquals(item, adapter.fetch(uri));
	}

    @Test
	public void testCanFetchResourcesForDailyMotionItems() throws Exception {
		
		Canonicaliser canonicaliser = new DailyMotionItemAdapter.DailyMotionItemCanonicaliser();
		
		assertFalse(adapter.canFetch("http://www.channel4.com/"));
		assertNull(canonicaliser.canonicalise("http://www.channel4.com/"));

		assertFalse(adapter.canFetch("http://www.dailymotion.com/"));
		assertTrue(adapter.canFetch("http://www.dailymotion.com/video/x9l8e7_protesting-irans-election_news"));
		
		
		assertFalse(adapter.canFetch("http://www.dailymotion.com/gb/featured/channel/fun/video/x9j0kq_what-do-you-want-on-your-tombstoney_fun"));
		assertEquals("http://www.dailymotion.com/video/x9j0kq_what-do-you-want-on-your-tombstoney_fun", canonicaliser.canonicalise("http://www.dailymotion.com/gb/featured/channel/fun/video/x9j0kq_what-do-you-want-on-your-tombstoney_fun"));
		
		assertFalse(adapter.canFetch("http://www.dailymotion.com/video"));
		assertNull(canonicaliser.canonicalise("http://www.dailymotion.com/video"));
	}
}
