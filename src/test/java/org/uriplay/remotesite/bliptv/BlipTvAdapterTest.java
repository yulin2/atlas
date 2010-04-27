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

package org.uriplay.remotesite.bliptv;

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
public class BlipTvAdapterTest extends MockObjectTestCase {
	
	static final String URI = "http://blip.tv/file/2276955";
	static final String VIDEO_SOURCE_URI = "http://e.blip.tv/scripts/flash/showplayer.swf?file=http://blip.tv/rss/flash/2289908";
	static final String EMBED_CODE = "<embed>...</embed>";
	
	RemoteSiteClient<HtmlDescriptionOfItem> itemClient;
	RemoteSiteClient<String> embedCodeClient;
	BeanGraphExtractor<HtmlDescriptionSource> propertyExtractor;
	BlipTvAdapter adapter;
	
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();
	
	HtmlDescriptionSource source = new HtmlDescriptionSource(item, URI).withEmbedCode(EMBED_CODE);
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		itemClient = mock(RemoteSiteClient.class, "itemClient");
		embedCodeClient = mock(RemoteSiteClient.class, "embedCodeClient");
		propertyExtractor = mock(BeanGraphExtractor.class);
		adapter = new BlipTvAdapter(itemClient, embedCodeClient, propertyExtractor);
		
		item.setVideoSource(VIDEO_SOURCE_URI);
	}
	
	public void testPerformsGetCorrespondingGivenUriThenRetrievesEmbedCodeAndPassesResultsToExtractor() throws Exception {
		
		checking(new Expectations() {{
			one(itemClient).get(URI); will(returnValue(item));
			one(embedCodeClient).get(VIDEO_SOURCE_URI); will(returnValue(EMBED_CODE));
			one(propertyExtractor).extractFrom(source);
		}});
		
		adapter.fetch(URI, null);
	}
	
	public void testCanFetchResourcesForBlipTvItems() throws Exception {
		
		assertFalse(adapter.canFetch("http://www.channel4.com/"));
		assertFalse(adapter.canFetch("http://blip.tv/"));
		assertTrue(adapter.canFetch("http://blip.tv/file/2114874?utm_source=episodepg_random&utm_medium=episodepg_random"));
		assertTrue(adapter.canFetch("http://blip.tv/file/2052465"));
	}
	
}
