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

package org.uriplay.remotesite.dailymotion;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

/**
 * Unit test for {@link C4BrandAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class DailyMotionItemAdapterTest extends MockObjectTestCase {
	
	String uri = "http://www.dailymotion.com/video/x9l8e7_protesting-irans-election_news";
	
	RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	BeanGraphExtractor<HtmlDescriptionSource> propertyExtractor;
	DailyMotionItemAdapter adapter;
	
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();
	
	HtmlDescriptionSource source = new HtmlDescriptionSource(item, uri);
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		itemClient = mock(RemoteSiteClient.class);
		propertyExtractor = mock(BeanGraphExtractor.class);
		adapter = new DailyMotionItemAdapter(itemClient, propertyExtractor);
	}
	
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		checking(new Expectations() {{
			one(itemClient).get(uri); will(returnValue(item));
			one(propertyExtractor).extractFrom(source);
		}});
		
		adapter.fetch(uri, null);
	}
	
	public void testCanFetchResourcesForDailyMotionItems() throws Exception {
		
		assertFalse(adapter.canFetch("http://www.channel4.com/"));
		assertFalse(adapter.canFetch("http://www.dailymotion.com/"));
		assertTrue(adapter.canFetch("http://www.dailymotion.com/video/x9l8e7_protesting-irans-election_news"));
		assertTrue(adapter.canFetch("http://www.dailymotion.com/gb/featured/channel/fun/video/x9j0kq_what-do-you-want-on-your-tombstoney_fun"));
		assertFalse(adapter.canFetch("http://www.dailymotion.com/video"));
	}
	
}
