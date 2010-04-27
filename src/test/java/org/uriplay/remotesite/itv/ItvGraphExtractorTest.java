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

package org.uriplay.remotesite.itv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ItvGraphExtractorTest extends MockObjectTestCase {
	
	
	static final String CATCHUP_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

	static final String BRAND_URI = "http://www.itv.com/ITVPlayer/Programmes/default.html?ViewType=1&Filter=2773";
	static final String EPISODE1_URI = "http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=100109";

	static final String EPISODE1_VERSION_ID = "1";
	static final String EPISODE1_WEB_ENCODING_ID = "2";
	static final String EPISODE1_WEB_LOCATION_ID = "3";

	static final String THUMBNAIL_URL = "http://itv.com/images/a.jpg";

	static final RequestTimer TIMER = null;
	
	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = mock(IdGenerator.class);
	
	ItvGraphExtractor extractor;

	ItvProgramme programme;

	ItvBrandSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		programme = new ItvProgramme(BRAND_URI).withThumbnail(THUMBNAIL_URL);
		programme.addEpisode(new ItvEpisode("14 Aug", "latest episode", EPISODE1_URI));
		
		source = new ItvBrandSource(Lists.newArrayList(programme), CATCHUP_URI);
		
		extractor = new ItvGraphExtractor(idGeneratorFactory);
		
		checking(new Expectations() {{
			allowing(idGeneratorFactory).create(); will(returnValue(idGenerator));
		}});
	}
	
	public void testCreatesEpisodesFromFeedEntries() throws Exception {

		checking(new Expectations() {{ 
			exactly(3).of(idGenerator).getNextId(); will(onConsecutiveCalls(returnValue(EPISODE1_VERSION_ID), returnValue(EPISODE1_WEB_ENCODING_ID),  returnValue(EPISODE1_WEB_LOCATION_ID)));
		}});
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Brand.class, representation.getType(BRAND_URI));
		assertThat(representation, hasPropertyValue(BRAND_URI, "publisher", "itv.com"));

		assertEquals(Episode.class, representation.getType(EPISODE1_URI));
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "description", "latest episode"));
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "publisher", "itv.com"));
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "curie", "itv:5-100109"));
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "isLongForm", true));
		assertThat(representation, hasPropertyValue(BRAND_URI, "curie", "itv:1-2773"));
		
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "containedIn", Sets.newHashSet(BRAND_URI)));
		
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "thumbnail", THUMBNAIL_URL));

		assertEquals(Version.class, representation.getType(EPISODE1_VERSION_ID));
		assertThat(representation, hasPropertyValue(EPISODE1_URI, "versions", Sets.newHashSet(EPISODE1_VERSION_ID)));
		
		assertThat(representation, hasPropertyValue(EPISODE1_VERSION_ID, "manifestedAs", Sets.newHashSet(EPISODE1_WEB_ENCODING_ID)));
		assertThat(representation.getAnonymous(), hasItem(EPISODE1_VERSION_ID));
		
		assertEquals(Encoding.class, representation.getType(EPISODE1_WEB_ENCODING_ID));
		assertThat(representation, hasPropertyValue(EPISODE1_WEB_ENCODING_ID, "availableAt", Sets.newHashSet(EPISODE1_WEB_LOCATION_ID)));

		assertEquals(Location.class, representation.getType(EPISODE1_WEB_LOCATION_ID));
		assertThat(representation.getAnonymous(), hasItem(EPISODE1_WEB_LOCATION_ID));
		
		assertThat(representation, hasPropertyValue(EPISODE1_WEB_LOCATION_ID, "uri", EPISODE1_URI));
		assertThat(representation, hasPropertyValue(EPISODE1_WEB_LOCATION_ID, "transportType", "htmlembed"));
		assertThat(representation, hasPropertyValue(EPISODE1_WEB_LOCATION_ID, "available", true));
	}

}
