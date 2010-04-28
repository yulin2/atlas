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

package org.uriplay.remotesite.synd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.core.MimeType;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Unit test for {@link GenericPodcastGraphExtractor}
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class GenericPodcastGraphExtractorTest extends MockObjectTestCase {

	static final String PODCAST_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml";
	static final String ENTRY_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3";
	static final String LOCATION_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3";
	
	static final String EPISODE_ID = "1";
	static final String VERSION_ID = "2";
	static final String ENCODING_ID = "3";
	static final String LOCATION_ID = "4";
	
	static final RequestTimer TIMER = null;
	
	IdGeneratorFactory idGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator idGenerator = mock(IdGenerator.class);
	
	GenericPodcastGraphExtractor extractor = new GenericPodcastGraphExtractor(idGeneratorFactory);
	SyndFeed feed;
	SyndicationSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feed = createFeed("BH", "Broadcasting House", LOCATION_URI, "audio/mpeg");
		source = new SyndicationSource(feed, PODCAST_URI, TIMER);
		
		checking(new Expectations() {{
			one(idGeneratorFactory).create(); will(returnValue(idGenerator));
			exactly(4).of(idGenerator).getNextId(); will(onConsecutiveCalls(returnValue(EPISODE_ID), returnValue(VERSION_ID), returnValue(ENCODING_ID), returnValue(LOCATION_ID)));
		}});
	}

	private SyndFeed createFeed(String title, String description, String uri, String type) {
		SyndFeed feed = new SyndFeedImpl();
		SyndEntry entry = new SyndEntryImpl();
		entry.setTitle(title);
		SyndContentImpl desc = new SyndContentImpl();
		desc.setValue(description);
		entry.setDescription(desc);
		entry.setLink(uri);
		SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
		enclosure.setLength(2048L);
		enclosure.setType(type);
		entry.setEnclosures(Lists.newArrayList(enclosure));
		feed.setEntries(Lists.newArrayList(entry));
		return feed;
	}
	
	public void testCanExtractTitleAndDescription() throws Exception {
		
		Representation representation = extractor.extractFrom(source);
		
		assertEquals(Playlist.class, representation.getType(PODCAST_URI));
		assertThat(representation, hasPropertyValue(PODCAST_URI, "items", Sets.newHashSet(EPISODE_ID)));
		assertEquals(Item.class, representation.getType(EPISODE_ID));
		assertThat(representation, hasPropertyValue(EPISODE_ID, "title", "BH"));
		assertThat(representation, hasPropertyValue(EPISODE_ID, "description", "Broadcasting House"));
		assertThat(representation, hasPropertyValue(EPISODE_ID, "containedIn", Sets.newHashSet(PODCAST_URI)));
	}
	
	public void testGeneratesVersionEncodingAndLocationData() throws Exception {
		
		Representation representation = extractor.extractFrom(source);

		assertThat(representation, hasPropertyValue(EPISODE_ID, "versions", Sets.newHashSet(VERSION_ID)));

		assertEquals(Version.class, representation.getType(VERSION_ID));
		assertThat(representation, hasPropertyValue(VERSION_ID, "manifestedAs", Sets.newHashSet(ENCODING_ID)));
		assertEquals(Encoding.class, representation.getType(ENCODING_ID));
		assertThat(representation.getAnonymous(), hasItem(ENCODING_ID));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "availableAt", Sets.newHashSet(LOCATION_ID)));
		assertEquals(Location.class, representation.getType(LOCATION_ID));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "transportType", TransportType.DOWNLOAD.toString()));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "transportSubType", "HTTP"));
		assertThat(representation, hasPropertyValue(LOCATION_ID, "uri", LOCATION_URI));
		
		assertThat(representation, hasPropertyValue(ENCODING_ID, "audioCoding", "audio/mpeg"));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataContainerFormat", MimeType.AUDIO_MPEG.toString()));
	}
	
	public void testSetsContainerFormatForVideoPodcasts() throws Exception {
		
		feed = createFeed("Video Podcast", "Rolling Stones", LOCATION_URI, "video/quicktime");
		source = new SyndicationSource(feed, PODCAST_URI, TIMER);
		
		Representation representation = extractor.extractFrom(source);
		assertThat(representation, not(hasPropertyValue(ENCODING_ID, "videoCoding", MimeType.VIDEO_XXVID.toString())));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataContainerFormat", MimeType.VIDEO_QUICKTIME.toString()));
	}
	
	public void testSetsVideoCodingForVideoMpegPodcasts() throws Exception {
		
		feed = createFeed("Video Podcast", "Rolling Stones", LOCATION_URI, "video/mpeg");
		source = new SyndicationSource(feed, PODCAST_URI, TIMER);
		
		Representation representation = extractor.extractFrom(source);
		assertThat(representation, hasPropertyValue(ENCODING_ID, "videoCoding", MimeType.VIDEO_MPEG.toString()));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataContainerFormat", MimeType.VIDEO_MPEG.toString()));
	}

}
