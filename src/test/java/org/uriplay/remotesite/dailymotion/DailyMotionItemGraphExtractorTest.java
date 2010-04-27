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

import com.google.common.collect.Sets;

/**
 * @author John Ayres (john@metabroadcast.com)
 */
public class DailyMotionItemGraphExtractorTest extends MockObjectTestCase {
	
	static final String ITEM_URI = "http://www.dailymotion.com/video/xbqomc_dont-do-anything_fun";
	
	static final String VERSION_URI = "1";
	static final String ENCODING_URI = "2";
	static final String LOCATION_URI = "3";

	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = new IntegerIdGenerator();
	
	DailyMotionItemGraphExtractor extractor;
	HtmlDescriptionSource source;
	HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		source = new HtmlDescriptionSource(item, ITEM_URI);
		extractor = new DailyMotionItemGraphExtractor(idGeneratorFactory);
		
		checking(new Expectations() {{
			one(idGeneratorFactory).create(); will(returnValue(idGenerator));
		}});
		
		item.setTitle("News News News");
		item.setDescription("The News");
		item.setThumbnail("thumbnail.image");
		item.setVideoSource("videoSource");
	}
	
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Episode.class, representation.getType(ITEM_URI));
		assertThat(representation, hasPropertyValue(ITEM_URI, "title", "News News News"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "description", "The News"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "publisher", "dailymotion.com"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "curie", "dm:xbqomc_dont-do-anything_fun"));
		
		assertThat(representation, hasPropertyValue(ITEM_URI, "thumbnail", "thumbnail.image"));

		assertEquals(Version.class, representation.getType(VERSION_URI));
		assertThat(representation, hasPropertyValue(VERSION_URI, "manifestedAs", Sets.newHashSet(ENCODING_URI)));

		assertEquals(Encoding.class, representation.getType(ENCODING_URI));
		assertThat(representation, hasPropertyValue(ENCODING_URI, "availableAt", Sets.newHashSet(LOCATION_URI)));
		
		assertEquals(Location.class, representation.getType(LOCATION_URI));
//		assertThat(representation, hasPropertyValue(LOCATION_URI, "uri", "videoSource"));
		assertThat(representation, hasPropertyValue(LOCATION_URI, "transportType", "embedobject"));
		assertThat(representation, hasPropertyValue(LOCATION_URI, "transportSubType", "html"));
	}
}
