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

package org.atlasapi.remotesite.oembed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.atlasapi.feeds.OembedItem;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.Iterables;

/**
 * Unit test for {@link OembedraphExtractor}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedGraphExtractorTest extends MockObjectTestCase {
	
	OembedItem item = createTestItem();
	
	String videoUri = "http://www.vimeo.com/1234";
	
	OembedSource source = new OembedSource(item, videoUri);
	
	
	private OembedItem createTestItem() {
		OembedItem testItem = new OembedItem();
		testItem.setTitle("Test Video Title");
		testItem.setProviderUrl("vimeo.com");
		testItem.setHeight(480);
		testItem.setWidth(640);
		testItem.setEmbedCode("<embed ...>");
		return testItem ;
	}

	public void testExtractsItemDetailsFromOembed() throws Exception {
		
		ContentExtractor<OembedSource, Item> extractor = new OembedGraphExtractor() {
			@Override
			protected String curieFor(String itemUri) {
				return "vim:1234";
			}
		};
		
		Item item = extractor.extract(source);
		
		assertThat(item.getCanonicalUri(), is(videoUri));
		
		assertThat(item.getTitle(), is("Test Video Title"));
		assertThat(item.getCurie(), is("vim:1234"));
	
		Version version = Iterables.getOnlyElement(item.getVersions());
		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
		Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
		
		assertThat(encoding.getVideoHorizontalSize(), is(640));
		assertThat(encoding.getVideoVerticalSize(), is(480));
		
		assertThat(location.getTransportType(), is(TransportType.EMBED));
		assertThat(location.getEmbedCode(), is("<embed ...>"));
	}
}
