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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.beans.id.IntegerIdGenerator;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BlipTvGraphExtractorTest extends MockObjectTestCase {
	
	static final String ITEM_URI = "http://blip.tv/file/2276955/";
	
	static final String VERSION_ID = "1";
	static final String ENCODING_1_ID = "2";
	static final String LOCATION_1_ID = "3";
	static final String LOCATION_2_ID = "4";

	static final String ENCODING_2_ID = "5";
	static final String LOCATION_3_ID = "6";
	
	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = new IntegerIdGenerator();
	
	BlipTvGraphExtractor extractor;
	HtmlDescriptionSource source;
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		source = new HtmlDescriptionSource(item, ITEM_URI);
		extractor = new BlipTvGraphExtractor(idGeneratorFactory);
		
		checking(new Expectations() {{
			one(idGeneratorFactory).create(); will(returnValue(idGenerator));
		}});
		
		item.setTitle("News News News");
		item.setDescription("The News");
		item.setThumbnail("thumbnail.image");
		item.setVideoSource("videoSource");
		
		item.setLocationUris(Lists.newArrayList("http://blip.tv/1.flv", "http://blip.tv/2.mov"));
	}
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Episode.class, representation.getType(ITEM_URI));
		assertThat(representation, hasPropertyValue(ITEM_URI, "title", "News News News"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "description", "The News"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "publisher", "blip.tv"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "curie", "blip:2276955"));
		
		assertThat(representation, hasPropertyValue(ITEM_URI, "thumbnail", "thumbnail.image"));

		assertEquals(Version.class, representation.getType(VERSION_ID));
		assertThat(representation, hasPropertyValue(VERSION_ID, "manifestedAs", Sets.newHashSet(ENCODING_1_ID, ENCODING_2_ID)));

		assertEquals(Encoding.class, representation.getType(ENCODING_1_ID));
		assertThat(representation, hasPropertyValue(ENCODING_1_ID, "availableAt", Sets.newHashSet(LOCATION_1_ID, LOCATION_2_ID)));
		assertThat(representation, hasPropertyValue(ENCODING_1_ID, "dataContainerFormat", "video/x-flv"));
		
		assertEquals(Location.class, representation.getType(LOCATION_1_ID));
		assertThat(representation, hasPropertyValue(LOCATION_1_ID, "uri", "http://blip.tv/1.flv"));
		assertThat(representation, hasPropertyValue(LOCATION_1_ID, "transportType", "download"));

		assertEquals(Location.class, representation.getType(LOCATION_2_ID));
		assertThat(representation, hasPropertyValue(LOCATION_2_ID, "transportType", "embedobject"));
		assertThat(representation, hasPropertyValue(LOCATION_2_ID, "transportSubType", "html"));
		
		assertEquals(Encoding.class, representation.getType(ENCODING_2_ID));
		assertThat(representation, hasPropertyValue(ENCODING_2_ID, "availableAt", Sets.newHashSet(LOCATION_3_ID)));
		assertThat(representation, hasPropertyValue(ENCODING_2_ID, "dataContainerFormat", "video/quicktime"));
		
		assertEquals(Location.class, representation.getType(LOCATION_3_ID));
		assertThat(representation, hasPropertyValue(LOCATION_3_ID, "uri", "http://blip.tv/2.mov"));
		assertThat(representation, hasPropertyValue(LOCATION_3_ID, "transportType", "download"));
	}

}
