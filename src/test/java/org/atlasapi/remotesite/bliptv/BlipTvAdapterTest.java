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

package org.atlasapi.remotesite.bliptv;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bliptv.BlipTvAdapter;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.html.HtmlDescriptionSource;
import org.jmock.integration.junit3.MockObjectTestCase;

/**
 * Unit test for {@link C4BrandAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class BlipTvAdapterTest extends MockObjectTestCase {
	
	static final String URI = "http://blip.tv/file/2276955";
	static final String VIDEO_SOURCE_URI = "http://e.blip.tv/scripts/flash/showplayer.swf?file=http://blip.tv/rss/flash/2289908";
	
	RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	ContentExtractor<HtmlDescriptionSource, Item> propertyExtractor;
	BlipTvAdapter adapter;
	
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		itemClient = mock(RemoteSiteClient.class, "itemClient");
		propertyExtractor = mock(ContentExtractor.class);
		adapter = new BlipTvAdapter(itemClient, propertyExtractor);
		
		item.setVideoSource(VIDEO_SOURCE_URI);
	}
		
	public void testCanFetchResourcesForBlipTvItems() throws Exception {
		
		Canonicaliser canonicaliser = new BlipTvAdapter.BlipTvCanonicaliser();
		
		assertFalse(adapter.canFetch("http://www.channel4.com/"));
		assertFalse(adapter.canFetch("http://blip.tv/"));
		
		assertFalse(adapter.canFetch("http://blip.tv/file/2114874?utm_source=episodepg_random&utm_medium=episodepg_random"));
		assertEquals("http://blip.tv/file/2114874", canonicaliser.canonicalise("http://blip.tv/file/2114874?utm_source=episodepg_random&utm_medium=episodepg_random"));
		
		assertTrue(adapter.canFetch("http://blip.tv/file/2052465"));
	}
	
}
