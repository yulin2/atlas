///* Copyright 2009 British Broadcasting Corporation
//   Copyright 2009 Meta Broadcast Ltd
//
//Licensed under the Apache License, Version 2.0 (the "License"); you
//may not use this file except in compliance with the License. You may
//obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//implied. See the License for the specific language governing
//permissions and limitations under the License. */
//
//package org.atlasapi.remotesite.synd;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//
//import org.atlasapi.media.TransportSubType;
//import org.atlasapi.media.TransportType;
//import org.atlasapi.media.content.Container;
//import org.atlasapi.media.entity.Encoding;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.media.entity.Location;
//import org.atlasapi.media.entity.Publisher;
//import org.atlasapi.media.entity.Version;
//import org.jmock.integration.junit3.MockObjectTestCase;
//
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;
//import com.metabroadcast.common.media.MimeType;
//import com.sun.syndication.feed.synd.SyndContentImpl;
//import com.sun.syndication.feed.synd.SyndEnclosureImpl;
//import com.sun.syndication.feed.synd.SyndEntry;
//import com.sun.syndication.feed.synd.SyndEntryImpl;
//import com.sun.syndication.feed.synd.SyndFeed;
//import com.sun.syndication.feed.synd.SyndFeedImpl;
//
///**
// * Unit test for {@link GenericPodcastGraphExtractor}
// *
// * @author Robert Chatley (robert@metabroadcast.com)
// */
//public class GenericPodcastGraphExtractorTest extends MockObjectTestCase {
//
//	private static final Publisher aPublisher = Publisher.BBC;
//	
//	static final String PODCAST_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml";
//	static final String ENTRY_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3";
//	static final String LOCATION_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3";
//		
//	GenericPodcastGraphExtractor extractor = new GenericPodcastGraphExtractor() {
//		@Override
//		protected String itemUri(SyndEntry entry) {
//			return entry.getLink();
//		}
//
//		@Override
//		protected Publisher publisher() {
//			return aPublisher;
//		}
//	};
//	
//	SyndFeed feed;
//	SyndicationSource source;
//
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		feed = createFeed("BH", "Broadcasting House", LOCATION_URI, "audio/mpeg");
//		source = new SyndicationSource(feed, PODCAST_URI);
//	}
//
//	private SyndFeed createFeed(String title, String description, String uri, String type) {
//		SyndFeed feed = new SyndFeedImpl();
//		SyndEntry entry = new SyndEntryImpl();
//		entry.setTitle(title);
//		SyndContentImpl desc = new SyndContentImpl();
//		desc.setValue(description);
//		entry.setDescription(desc);
//		entry.setLink(uri);
//		SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
//		enclosure.setLength(2048L);
//		enclosure.setType(type);
//		entry.setEnclosures(Lists.newArrayList(enclosure));
//		feed.setEntries(Lists.newArrayList(entry));
//		return feed;
//	}
//	
//	public void testCanExtractTitleAndDescription() throws Exception {
//		
//		Container<?> playlist = extractor.extract(source);
//		assertThat(playlist.getCanonicalUri(), is(PODCAST_URI));
//		assertThat(playlist.getPublisher(), is(aPublisher));
//		
//		Item item = Iterables.getOnlyElement(playlist.getContents());
//		
//		assertThat(item.getCanonicalUri(), is(ENTRY_URI));
//		
//		assertThat(item.getTitle(), is("BH"));
//		assertThat(item.getDescription(), is("Broadcasting House"));
//	
//		Version version = Iterables.getOnlyElement(item.getVersions());
//		
//		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
//
//		assertThat(encoding.getAudioCoding(), is(MimeType.AUDIO_MPEG));
//		assertThat(encoding.getDataContainerFormat(), is(MimeType.AUDIO_MPEG));
//
//		Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
//
//		assertThat(location.getTransportType(), is(TransportType.DOWNLOAD));
//		assertThat(location.getTransportSubType(), is(TransportSubType.HTTP));
//		assertThat(location.getUri(), is(LOCATION_URI));
//		
//	}
//	
//	public void testSetsContainerFormatForVideoPodcasts() throws Exception {
//		
//		feed = createFeed("Video Podcast", "Rolling Stones", LOCATION_URI, "video/quicktime");
//		source = new SyndicationSource(feed, PODCAST_URI);
//		
//		Encoding encoding = extractEncodingFrom(source);
//		
//		assertThat(encoding.getVideoCoding(), is(not(MimeType.VIDEO_XXVID)));
//		assertThat(encoding.getDataContainerFormat(), is(MimeType.VIDEO_QUICKTIME));
//	}
//
//	private Encoding extractEncodingFrom(SyndicationSource source) {
//		Container<?> playlist = extractor.extract(source);
//		Item item = Iterables.getOnlyElement(playlist.getContents());
//		Version version = Iterables.getOnlyElement(item.getVersions());
//		return Iterables.getOnlyElement(version.getManifestedAs());
//	}
//	
//	public void testSetsVideoCodingForVideoMpegPodcasts() throws Exception {
//		
//		feed = createFeed("Video Podcast", "Rolling Stones", LOCATION_URI, "video/mpeg");
//		source = new SyndicationSource(feed, PODCAST_URI);
//		
//		Encoding encoding = extractEncodingFrom(source);
//		
//		assertThat(encoding.getVideoCoding(), is(MimeType.VIDEO_MPEG));
//		assertThat(encoding.getDataContainerFormat(), is(MimeType.VIDEO_MPEG));
//	}
//
//}
