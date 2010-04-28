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

package org.uriplay.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Unit test for {@link BbcPodcastGraphExtractor}
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcPodcastGraphExtractorTest extends MockObjectTestCase {

	static final String PODCAST_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml";
	static final String LOCATION_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3";
	static final String SLASH_PROGRAMMES_URI = "http://www.bbc.co.uk/programmes/b00xxx";
	
	static final String EPISODE_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900";

	static final String VERSION_ID = "1";
	static final String ENCODING_ID = "2";
	static final String LOCATION_ID = "3";
	
	static final RequestTimer TIMER = null;
	
	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = mock(IdGenerator.class);
	
	BbcPodcastGraphExtractor extractor = new BbcPodcastGraphExtractor(idGeneratorFactory);
	SyndFeed feed;
	SyndicationSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feed = createFeed("BH", "Broadcasting House", "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3");
		source = new SyndicationSource(feed, PODCAST_URI, TIMER);
	
		checking(new Expectations() {{
			one(idGeneratorFactory).create(); will(returnValue(idGenerator));
			exactly(3).of(idGenerator).getNextId(); will(onConsecutiveCalls(returnValue(VERSION_ID), returnValue(ENCODING_ID), returnValue(LOCATION_ID)));
		}});
	}

	@SuppressWarnings("unchecked")
	private SyndFeed createFeed(String title, String description, String uri) {
		SyndFeed feed = new SyndFeedImpl();
		SyndEntry entry = new SyndEntryImpl();
		entry.setTitle(title);
		SyndContentImpl desc = new SyndContentImpl();
		desc.setValue(description);
		entry.setDescription(desc);
		entry.setUri(uri);
		entry.setLink(LOCATION_URI);
		SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
		enclosure.setLength(2048L);
		enclosure.setType("audio/mpeg");
		entry.setEnclosures(Lists.newArrayList(enclosure));
		feed.setEntries(Lists.newArrayList(entry));
		Element sysRef = new Element("systemRef", "ppg", "http://www.bbc.co.uk");
		sysRef.getAttributes().add(new Attribute("systemId", "pid.brand"));
		sysRef.getAttributes().add(new Attribute("key", "b00xxx"));
		feed.setForeignMarkup(Lists.newArrayList(sysRef));
		return feed;
	}
	
	public void testCanExtractTitleDescriptionAndPid() throws Exception {
		
		Representation representation = extractor.extractFrom(source);
		
		assertEquals(Brand.class, representation.getType(PODCAST_URI));
		assertThat(representation, hasPropertyValue(PODCAST_URI, "items", Sets.newHashSet(EPISODE_URI)));
		//assertThat(representation, hasPropertyValue(PODCAST_URI, "aliases", Sets.newHashSet(SLASH_PROGRAMMES_URI)));
		assertEquals(Item.class, representation.getType(EPISODE_URI));
		assertThat(representation, hasPropertyValue(EPISODE_URI, "title", "BH"));
		assertThat(representation, hasPropertyValue(EPISODE_URI, "description", "Broadcasting House"));
		assertThat(representation, hasPropertyValue(EPISODE_URI, "containedIn", Sets.newHashSet(PODCAST_URI)));
	}
	
	public void testGeneratesVersionEncodingAndLocationData() throws Exception {
		
		Representation representation = extractor.extractFrom(source);
		
		assertThat(representation, hasPropertyValue(EPISODE_URI, "versions", Sets.newHashSet(VERSION_ID)));
		assertThat(representation, hasPropertyValue(EPISODE_URI, "publisher", "bbc.co.uk"));
		assertEquals(Version.class, representation.getType(VERSION_ID));

		assertThat(representation, hasPropertyValue(VERSION_ID, "manifestedAs", Sets.newHashSet(ENCODING_ID)));
		assertEquals(Encoding.class, representation.getType(ENCODING_ID));
		
		assertThat(representation, hasPropertyValue(ENCODING_ID, "availableAt", Sets.newHashSet(LOCATION_ID)));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataSize", 2L));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "audioCoding", "audio/mpeg"));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataContainerFormat", "audio/mpeg"));
	
		assertEquals(Location.class, representation.getType(LOCATION_ID));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "transportType", TransportType.DOWNLOAD.toString()));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "transportSubType", "HTTP"));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "uri", LOCATION_URI));
	}
	
	public void testDealsWithChrisMoylesEnhancedPodcastAsMp4() throws Exception {
		Representation representation = extractor.extractFrom(moylesSource());
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataContainerFormat", "audio/mp4"));
	}

	private SyndicationSource moylesSource() {
		SyndFeed feed = new SyndFeedImpl();
		SyndEntry entry = new SyndEntryImpl();
		entry.setTitle("Moyles");
		SyndContentImpl desc = new SyndContentImpl();
		desc.setValue("Moyles enhanced podcast");
		entry.setDescription(desc);
		entry.setUri("http://downloads.bbc.co.uk/podcasts/radio1/moylesen/rss.xml");
		entry.setLink(LOCATION_URI);
		SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
		enclosure.setLength(2048L);
		enclosure.setType("audio/x-m4a");
		entry.setEnclosures(Lists.newArrayList(enclosure));
		feed.setEntries(Lists.newArrayList(entry));
		return new SyndicationSource(feed, PODCAST_URI, TIMER);
	}

}
