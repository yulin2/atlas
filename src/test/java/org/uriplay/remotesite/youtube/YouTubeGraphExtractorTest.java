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

package org.uriplay.remotesite.youtube;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.jherd.hamcrest.Matchers.hasPropertyValue;

import java.util.List;
import java.util.Set;

import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.youtube.VideoEntry;

/**
 * Unit test for {@link YouTubeGraphExtractor}
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGraphExtractorTest extends MockObjectTestCase {

	static final String ITEM_URI = "http://www.youtube.com/watch?v=otA7tjinFX4";
	static final String VERSION_ID = "1";
	static final String LOCATION_URI = "http://www.youtube.com/v/otA7tjinFX4";
	static final String LOCATION_URI_2 = "http://www.youtube.com/v/svnwenn331";
	static final String LOCATION_URI_3 = "http://www.youtube.com/v/svnwvskld31";

	static final String ENCODING_ID = "2";
	static final String LOCATION_ID_1 = "3";

	static final String ENCODING_ID_2 = "4";
	static final String LOCATION_ID_2 = "5";

	static final String ENCODING_ID_3 = "6";
	static final String LOCATION_ID_3 = "7";
	
	static final String ENCODING_ID_4 = "8";
	static final String LOCATION_ID_4 = "9";

	static final String THUMBNAIL_URI = "http://i.ytimg.com/vi/otA7tjinFX4/3.jpg";
	static final String IMAGE_URI = "http://i.ytimg.com/vi/otA7tjinFX4/3thumb.jpg";
	
	IdGeneratorFactory mockIdGeneratorFactory = mock(IdGeneratorFactory.class);
	IdGenerator mockIdGenerator = mock(IdGenerator.class);
	YouTubeGraphExtractor extractor = new YouTubeGraphExtractor(mockIdGeneratorFactory);
	VideoEntry entry = new VideoEntry();
	YouTubeSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		entry.setTitle(text("Video Title"));
		source = new TestYouTubeSource(entry, ITEM_URI);
	}
	
	class TestYouTubeSource extends YouTubeSource {

		public TestYouTubeSource(VideoEntry entry, String episodeUri) {
			super(entry, episodeUri);
		}
		
		@Override
		List<Video> getVideos() {
			return Lists.newArrayList(new Video("application/x-shockwave-flash", 300, LOCATION_URI, 5, true), 
									  new Video("video/3gpp", 300, LOCATION_URI_2, 1, true),
					                  new Video("video/3gpp", 300, LOCATION_URI_3, 6, true));
		}
		
		@Override
		Set<String> getCategories() {
			return Sets.newHashSet("http://uriplay.org/genres/youtube/News");
		}
		
		@Override
		Set<String> getTags() {
			return Sets.newHashSet("http://uriplay.org/tags/funny");
		}
		
		@Override
		String getDescription() {
			return "Description of video";
		}
		
		@Override
		public String getThumbnailImageUri() {
			return THUMBNAIL_URI;
		}
		
		@Override
		public String getImageUri() {
			return IMAGE_URI;
		}
	}
	
	public void testCanExtractVideoTitleDescriptionCategories() throws Exception {
		
		checking(new Expectations() {{
			allowing(mockIdGeneratorFactory).create(); will(returnValue(mockIdGenerator));
			ignoring(mockIdGenerator);
		}});
		
		Representation representation = extractor.extractFrom(source);
		
		assertEquals(Item.class, representation.getType(ITEM_URI));
		assertThat(representation, hasPropertyValue(ITEM_URI, "title", "Video Title"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "description", "Description of video"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "genres", Sets.newHashSet("http://uriplay.org/genres/youtube/News", "http://uriplay.org/genres/uriplay/news")));
		assertThat(representation, hasPropertyValue(ITEM_URI, "tags", Sets.newHashSet("http://uriplay.org/tags/funny")));
		assertThat(representation, hasPropertyValue(ITEM_URI, "publisher", "youtube.com"));
		assertThat(representation, hasPropertyValue(ITEM_URI, "thumbnail", THUMBNAIL_URI));
		assertThat(representation, hasPropertyValue(ITEM_URI, "image", IMAGE_URI));
		assertThat(representation, hasPropertyValue(ITEM_URI, "curie", "yt:otA7tjinFX4"));
	}
	
	public void testGeneratesVersionEncodingAndLocationData() throws Exception {
	
		checking(new Expectations() {{
			one(mockIdGeneratorFactory).create(); will(returnValue(mockIdGenerator));
			exactly(9).of(mockIdGenerator).getNextId(); 
				will(onConsecutiveCalls(returnValue(ENCODING_ID), returnValue(LOCATION_ID_1), returnValue(ENCODING_ID_2), 
										returnValue(LOCATION_ID_2), returnValue(ENCODING_ID_3), returnValue(LOCATION_ID_3), returnValue(ENCODING_ID_4), returnValue(LOCATION_ID_4), returnValue(VERSION_ID)));
		}});
		
		Representation representation = extractor.extractFrom(source);

		assertEquals(Version.class, representation.getType(VERSION_ID));
		assertThat(representation.getAnonymous(), hasItem(VERSION_ID));
		
		assertThat(representation, hasPropertyValue(ITEM_URI, "versions", Sets.newHashSet(VERSION_ID)));
		assertThat(representation, hasPropertyValue(VERSION_ID, "manifestedAs", Sets.newHashSet(ENCODING_ID, ENCODING_ID_2, ENCODING_ID_3, ENCODING_ID_4)));
		assertThat(representation, hasPropertyValue(VERSION_ID, "duration", 300));
		assertThat(representation, hasPropertyValue(ITEM_URI, "isLongForm", false));
		
		assertEquals(Encoding.class, representation.getType(ENCODING_ID));
		assertThat(representation.getAnonymous(), hasItem(ENCODING_ID));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "availableAt", Sets.newHashSet(LOCATION_ID_1)));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "dataContainerFormat", "application/x-shockwave-flash"));
		assertThat(representation, not(hasPropertyValue(ENCODING_ID, "videoCoding", "video/x-vp6")));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "hasDOG", true));
	
		assertEquals(Location.class, representation.getType(LOCATION_ID_1));
		assertThat(representation, hasPropertyValue(LOCATION_ID_1, "transportType", TransportType.EMBEDOBJECT.toString()));
		assertThat(representation, hasPropertyValue(LOCATION_ID_1, "transportSubType", "html"));
		
		assertEquals(Encoding.class, representation.getType(ENCODING_ID_2));
		assertThat(representation.getAnonymous(), hasItem(ENCODING_ID_2));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "availableAt", Sets.newHashSet(LOCATION_ID_2)));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "videoHorizontalSize", 176));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "videoVerticalSize", 144));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "dataContainerFormat", "video/3gpp"));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "videoCoding", "video/H263"));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "audioCoding", "audio/AMR"));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "audioChannels", 1));
		assertThat(representation, hasPropertyValue(ENCODING_ID_2, "hasDOG", false));
		assertEquals(Location.class, representation.getType(LOCATION_ID_2));

		assertThat(representation, hasPropertyValue(LOCATION_ID_2, "transportType", "stream"));
		assertThat(representation, hasPropertyValue(LOCATION_ID_2, "transportSubType", "rtsp"));
		
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "videoHorizontalSize", 176));
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "videoVerticalSize", 144));
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "dataContainerFormat", "video/3gpp"));
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "videoCoding", "video/H263"));
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "audioCoding", "audio/mp4"));
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "audioChannels", 1));
		assertThat(representation, hasPropertyValue(ENCODING_ID_3, "hasDOG", false));
		
		assertThat(representation, hasPropertyValue(LOCATION_ID_3, "transportType", "stream"));
		assertThat(representation, hasPropertyValue(LOCATION_ID_3, "transportSubType", "rtsp"));
		
		assertEquals(Encoding.class, representation.getType(ENCODING_ID_3));
		assertThat(representation.getAnonymous(), hasItem(ENCODING_ID_3));
		assertThat(representation, hasPropertyValue(ENCODING_ID_4, "availableAt", Sets.newHashSet(LOCATION_ID_4)));
		assertEquals(Location.class, representation.getType(LOCATION_ID_1));
		assertThat(representation.getAnonymous(), hasItem(LOCATION_ID_1));
		assertThat(representation, hasPropertyValue(LOCATION_ID_4, "uri", ITEM_URI));
	}

	private TextConstruct text(String text) {
		
		return TextConstruct.create(TextConstruct.Type.TEXT, text, null);
	}
	
	class NoVideosYouTubeSource extends YouTubeSource {

		public NoVideosYouTubeSource(VideoEntry entry, String episodeUri) {
			super(entry, episodeUri);
		}
		
		@Override
		List<Video> getVideos() {
			return Lists.newArrayList();
		}
		
		@Override
		Set<String> getCategories() {
			return Sets.newHashSet();
		}
		
		@Override
		Set<String> getTags() {
			return Sets.newHashSet();
		}
		
		@Override
		String getDescription() {
			return "Description of video";
		}
		
		@Override
		public String getThumbnailImageUri() {
			return THUMBNAIL_URI;
		}
		
		@Override
		public String getImageUri() {
			return IMAGE_URI;
		}
	}
	
	public void testCreatesLocationForWebPageEvenWhenNoVideosReturnedBySource() throws Exception {
		
		checking(new Expectations() {{
			one(mockIdGeneratorFactory).create(); will(returnValue(mockIdGenerator));
			exactly(3).of(mockIdGenerator).getNextId(); 
				will(onConsecutiveCalls(returnValue(ENCODING_ID), returnValue(LOCATION_ID_1), returnValue(VERSION_ID)));
		}});
		
		source = new NoVideosYouTubeSource(entry, ITEM_URI);
		
		Representation representation = extractor.extractFrom(source);
		
		assertEquals(Item.class, representation.getType(ITEM_URI));
		assertEquals(Encoding.class, representation.getType(ENCODING_ID));
		assertEquals(Location.class, representation.getType(LOCATION_ID_1));
		assertEquals(Version.class, representation.getType(VERSION_ID));
		assertThat(representation, hasPropertyValue(ITEM_URI, "versions", Sets.newHashSet(VERSION_ID)));
		assertThat(representation, hasPropertyValue(VERSION_ID, "manifestedAs", Sets.newHashSet(ENCODING_ID)));
		assertThat(representation, hasPropertyValue(ENCODING_ID, "availableAt", Sets.newHashSet(LOCATION_ID_1)));
		assertThat(representation, hasPropertyValue(LOCATION_ID_1, "transportType", TransportType.HTMLEMBED.toString()));
	}

}
