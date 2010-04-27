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

package org.uriplay.remotesite.oembed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.feeds.OembedItem;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Sets;

/**
 * Unit test for {@link OembedraphExtractor}.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OembedGraphExtractorTest extends MockObjectTestCase {
	
	static final String LOCATION_ID = "1";
	static final String ENCODING_ID = "3";
	static final String VERSION_ID  = "5";
	
	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = mock(IdGenerator.class);
	
	OembedItem item = createTestItem();
	
	String videoUri = "http://www.vimeo.com/1234";
	
	OembedSource source = new OembedSource(item, videoUri);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		checking(new Expectations() {{
			allowing(idGeneratorFactory).create(); will(returnValue(idGenerator));
		}});
	}
	
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
		
		checking(new Expectations() {{ 
			exactly(3).of(idGenerator).getNextId(); will(onConsecutiveCalls(returnValue(VERSION_ID), returnValue(ENCODING_ID), returnValue(LOCATION_ID))); 
		}});
		
		BeanGraphExtractor<OembedSource> extractor = new OembedGraphExtractor(idGeneratorFactory) {
			@Override
			protected String curieFor(String itemUri) {
				return "vim:1234";
			}
		};
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Episode.class, representation.getType(videoUri));
		assertThat(representation, hasPropertyValue(videoUri, "title", "Test Video Title"));
		assertThat(representation, hasPropertyValue(videoUri, "publisher", "vimeo.com"));
		assertThat(representation, hasPropertyValue(videoUri, "curie", "vim:1234"));
	
		assertEquals(Version.class, representation.getType(VERSION_ID));
		assertThat(representation, hasPropertyValue(videoUri, "versions", Sets.newHashSet(VERSION_ID)));
		
		assertEquals(Encoding.class, representation.getType(ENCODING_ID));
		assertThat(representation, hasPropertyValue(VERSION_ID, "manifestedAs", Sets.newHashSet(ENCODING_ID)));
		
		assertEquals(Location.class, representation.getType(LOCATION_ID));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "availableAt", Sets.newHashSet(LOCATION_ID)));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "videoHorizontalSize", 640));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "videoVerticalSize", 480));
		
		assertThat(representation, hasPropertyValue(LOCATION_ID, "transportType", "embedobject"));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "transportSubType", "html"));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "embedCode", "<embed ...>"));
	}

}
