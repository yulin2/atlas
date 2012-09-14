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
//package org.atlasapi.remotesite.bbc;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//
//import org.atlasapi.media.TransportSubType;
//import org.atlasapi.media.TransportType;
//import org.atlasapi.media.content.Container;
//import org.atlasapi.media.entity.Encoding;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.media.entity.Location;
//import org.atlasapi.media.entity.MediaType;
//import org.atlasapi.media.entity.Publisher;
//import org.atlasapi.media.entity.Version;
//import org.atlasapi.remotesite.synd.SyndicationSource;
//import org.jdom.Attribute;
//import org.jdom.Element;
//import org.jmock.integration.junit3.MockObjectTestCase;
//
//import com.google.common.collect.ImmutableList;
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
// * Unit test for {@link BbcPodcastGraphExtractor}
// *
// * @author Robert Chatley (robert@metabroadcast.com)
// */
//public class BbcPodcastGraphExtractorTest extends MockObjectTestCase {
//
//	static final String PODCAST_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/rss.xml";
//	static final String LOCATION_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3";
//	static final String SLASH_PROGRAMMES_URI = "http://www.bbc.co.uk/programmes/b00xxx";
//	
//	static final String EPISODE_URI = "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900";
//	
//	
//	BbcPodcastGraphExtractor extractor = new BbcPodcastGraphExtractor();
//	SyndFeed feed;
//	SyndicationSource source;
//
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		feed = createFeed("BH", "Broadcasting House", "http://downloads.bbc.co.uk/podcasts/radio4/bh/bh_20090125-0900.mp3");
//		source = new SyndicationSource(feed, PODCAST_URI);
//	}
//
//	@SuppressWarnings("unchecked")
//	private SyndFeed createFeed(String title, String description, String uri) {
//		SyndFeed feed = new SyndFeedImpl();
//		SyndEntry entry = new SyndEntryImpl();
//		entry.setTitle(title);
//		SyndContentImpl desc = new SyndContentImpl();
//		desc.setValue(description);
//		entry.setDescription(desc);
//		entry.setUri(uri);
//		entry.setLink(LOCATION_URI);
//		SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
//		enclosure.setLength(2048L);
//		enclosure.setType("audio/mpeg");
//		entry.setEnclosures(Lists.newArrayList(enclosure));
//		feed.setEntries(Lists.newArrayList(entry));
//		Element sysRef = new Element("systemRef", "ppg", "http://www.bbc.co.uk");
//		sysRef.getAttributes().add(new Attribute("systemId", "pid.brand"));
//		sysRef.getAttributes().add(new Attribute("key", "b00xxx"));
//		Element svcRef = new Element("network", "ppg", "http://www.bbc.co.uk");
//		svcRef.getAttributes().add(new Attribute("id", "radio4"));
//		svcRef.getAttributes().add(new Attribute("name", "BBC Radio 4"));
//		feed.setForeignMarkup(ImmutableList.of(sysRef, svcRef));
//		return feed;
//	}
//	
//	public void testCanExtractTitleDescriptionAndPid() throws Exception {
//		
//		Container<Item> playlist = extractor.extract(source);
//		
//		Item item = Iterables.getOnlyElement(playlist.getContents());
//		
//		assertThat(item.getTitle(), is("BH"));
//		assertThat(item.getPublisher(), is(Publisher.BBC));
//		assertThat(item.getDescription(), is("Broadcasting House"));
//	}
//	
//	public void testGeneratesVersionEncodingAndLocationData() throws Exception {
//		
//		Container<Item> playlist = extractor.extract(source);
//		
//		Item item = Iterables.getOnlyElement(playlist.getContents());
//		Version version = Iterables.getOnlyElement(item.getVersions());
//		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
//		Location location  = Iterables.getOnlyElement(encoding.getAvailableAt());
//		
//		assertThat(encoding.getDataSize(), is(2L));
//		assertThat(encoding.getAudioCoding(), is(MimeType.AUDIO_MPEG));
//		assertThat(encoding.getDataContainerFormat(), is(MimeType.AUDIO_MPEG));
//	
//		assertThat(location.getTransportType(), is(TransportType.DOWNLOAD));
//		assertThat(location.getTransportSubType(), is(TransportSubType.HTTP));
//		assertThat(location.getUri(), is(LOCATION_URI));
//	}
//	
//	public void testDealsWithChrisMoylesEnhancedPodcastAsMp4() throws Exception {
//		Container<Item> playlist = extractor.extract(moylesSource());
//		Item item = Iterables.getOnlyElement(playlist.getContents());
//		Version version = Iterables.getOnlyElement(item.getVersions());
//		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
//		assertThat(encoding.getDataContainerFormat(), is(MimeType.AUDIO_MP4));
//	}
//
//	private SyndicationSource moylesSource() {
//		SyndFeed feed = new SyndFeedImpl();
//		SyndEntry entry = new SyndEntryImpl();
//		entry.setTitle("Moyles");
//		SyndContentImpl desc = new SyndContentImpl();
//		desc.setValue("Moyles enhanced podcast");
//		entry.setDescription(desc);
//		entry.setUri("http://downloads.bbc.co.uk/podcasts/radio1/moylesen/rss.xml");
//		entry.setLink(LOCATION_URI);
//		SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
//		enclosure.setLength(2048L);
//		enclosure.setType("audio/x-m4a");
//		entry.setEnclosures(Lists.newArrayList(enclosure));
//		feed.setEntries(Lists.newArrayList(entry));
//		return new SyndicationSource(feed, PODCAST_URI);
//	}
//
//	public void testSetsContentType() {
//		Container<Item> playlist = extractor.extract(source);
//		Item item = Iterables.getOnlyElement(playlist.getContents());
//
//		assertThat(playlist.getMediaType(), is(MediaType.AUDIO));
//		assertThat(item.getMediaType(), is(MediaType.AUDIO));		
//	}
//}
