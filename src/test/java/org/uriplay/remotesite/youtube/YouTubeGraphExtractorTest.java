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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Iterables;
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

	static final String LOCATION_URI = "http://www.youtube.com/v/otA7tjinFX4";
	static final String LOCATION_URI_2 = "http://www.youtube.com/v/svnwenn331";
	static final String LOCATION_URI_3 = "http://www.youtube.com/v/svnwvskld31";

	static final String THUMBNAIL_URI = "http://i.ytimg.com/vi/otA7tjinFX4/3.jpg";
	static final String IMAGE_URI = "http://i.ytimg.com/vi/otA7tjinFX4/3thumb.jpg";
	
	YouTubeGraphExtractor extractor = new YouTubeGraphExtractor();
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
		
		Item item = extractor.extract(source);
		assertThat(item.getCanonicalUri(), is(ITEM_URI));
		assertThat(item.getTitle(), is("Video Title"));
		assertThat(item.getDescription(), is("Description of video"));
		assertThat(item.getGenres(), is((Set<String>) Sets.<String>newHashSet("http://uriplay.org/genres/youtube/News", "http://uriplay.org/genres/uriplay/news")));
		assertThat(item.getTags(), is((Set<String>) Sets.<String>newHashSet("http://uriplay.org/tags/funny")));
		assertThat(item.getPublisher(), is("youtube.com"));
		assertThat(item.getThumbnail(),  is(THUMBNAIL_URI));
		assertThat(item.getImage(), is(IMAGE_URI));
		assertThat(item.getCurie(),  is("yt:otA7tjinFX4"));
	}
	
	@SuppressWarnings("unchecked")
	public void testGeneratesVersionEncodingAndLocationData() throws Exception {
	
		Item item = extractor.extract(source);
		assertThat(item.getCanonicalUri(), is(ITEM_URI));
		assertThat(item.getIsLongForm(), is(false));

		Version version = Iterables.getOnlyElement(item.getVersions());
		assertThat(version.getDuration(), is(300));
		
		Set<Encoding> encodings = version.getManifestedAs();
		assertThat(encodings.size(), is(4));
		
		Matcher<Encoding> encoding1 = 
			new EncodingMatcher()
				.withDataContainerFormat(is("application/x-shockwave-flash"))
				.withVideoCoding(is(not("video/x-vp6")))
				.withDOG(is(true))
				.withLocations(hasItems(
						new LocationMatcher()
							.withTransportSubType(is("html"))
							.withTransportType(is(TransportType.EMBEDOBJECT.toString().toLowerCase()))));
		
		
		Matcher<Encoding> encoding2 = 
			new EncodingMatcher()
				.withDataContainerFormat(is("video/3gpp"))
				.withVideoCoding(is("video/H263"))
				.withAudioCoding(is("audio/AMR"))
				.withVideoHorizonalSize(is(176))
				.withVideoVerticalSize(is(144))
				.withAudioChannels(is(1))
				.withDOG(is(false))
				.withLocations(hasItems(
						new LocationMatcher()
							.withTransportSubType(is("rtsp"))
							.withTransportType(is(TransportType.STREAM.toString().toLowerCase()))));
		
		Matcher<Encoding> encoding3 = 
			new EncodingMatcher()
				.withDataContainerFormat(is("video/3gpp"))
				.withVideoCoding(is("video/H263"))
				.withAudioCoding(is("audio/mp4")) 
				.withVideoHorizonalSize(is(176)) 
				.withVideoVerticalSize(is(144)) 
				.withAudioChannels(is(1)) 
				.withDOG(is(false)) 
				.withLocations(hasItems(
						new LocationMatcher()
							.withTransportSubType(is("rtsp"))
							.withTransportType(is(TransportType.STREAM.toString().toLowerCase()))));
		
		Matcher<Encoding> encoding4 = 
			new EncodingMatcher()
				.withLocations(hasItems(
						new LocationMatcher()
							.withUri(is(ITEM_URI))
							.withTransportType(is(TransportType.HTMLEMBED.toString().toLowerCase()))));
		
		assertThat(encodings, hasItems(encoding1, encoding2, encoding3, encoding4));
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
		
		source = new NoVideosYouTubeSource(entry, ITEM_URI);
		
		Item item = extractor.extract(source);
		assertThat(item.getCanonicalUri(), is(ITEM_URI));
		
		Version version = Iterables.getOnlyElement(item.getVersions());
		Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
		Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
		
		assertThat(location.getTransportType(), is(TransportType.HTMLEMBED.toString().toLowerCase()));
	}

	static class EncodingMatcher extends TypeSafeMatcher<Encoding> {

		private Matcher<String> dataContainerFormatMatcher;
		private Matcher<String> audioCodingMatcher;
		private Matcher<String> videoCodingMatcher;
		private Matcher<Boolean> dogMatcher;
		private Matcher<Integer> videoHorizonalSizeMatcher;
		private Matcher<Integer> videoVerticalSizeMatcher;
		private Matcher<Iterable<Location>> locationMatcher;
		private Matcher<Integer> audioChannelsMatcher;

		public EncodingMatcher withDataContainerFormat(Matcher<String> dataContainerFormatMatcher) {
			this.dataContainerFormatMatcher = dataContainerFormatMatcher;
			return this;
		}
		
		public EncodingMatcher withDOG(Matcher<Boolean> dogMatcher) {
			this.dogMatcher = dogMatcher;
			return this;
		}

		public EncodingMatcher withAudioCoding(Matcher<String> audioCodingMatcher) {
			this.audioCodingMatcher = audioCodingMatcher;
			return this;
		}
		
		public EncodingMatcher withVideoCoding(Matcher<String> videoCodingMatcher) {
			this.videoCodingMatcher = videoCodingMatcher;
			return this;
		}
		
		public EncodingMatcher withVideoHorizonalSize(Matcher<Integer> withVideoHorizonalSizeMatcher) {
			this.videoHorizonalSizeMatcher = withVideoHorizonalSizeMatcher;
			return this;
		}
		
		public EncodingMatcher withVideoVerticalSize(Matcher<Integer> videoVerticalSizeMatcher) {
			this.videoVerticalSizeMatcher = videoVerticalSizeMatcher;
			return this;
		}
		
		public EncodingMatcher withAudioChannels(Matcher<Integer> audioChannelsMatcher) {
			this.audioChannelsMatcher = audioChannelsMatcher;
			return this;
		}
		
		
		public EncodingMatcher withLocations(Matcher<Iterable<Location>> locationMatcher) {
			this.locationMatcher = locationMatcher;
			return this;
		}
		
		@Override
		public boolean matchesSafely(Encoding encoding) {
			if (dataContainerFormatMatcher != null && ! dataContainerFormatMatcher.matches(encoding.getDataContainerFormat())) {
				return false;
			}
			if (audioCodingMatcher != null && ! audioCodingMatcher.matches(encoding.getAudioCoding())) {
				return false;
			}
			if (videoCodingMatcher != null && ! videoCodingMatcher.matches(encoding.getVideoCoding())) {
				return false;
			}
			if (videoVerticalSizeMatcher != null && ! videoVerticalSizeMatcher.matches(encoding.getVideoVerticalSize())) {
				return false;
			}
			if (videoHorizonalSizeMatcher != null && ! videoHorizonalSizeMatcher.matches(encoding.getVideoHorizontalSize())) {
				return false;
			}
			
			if (audioChannelsMatcher != null && ! audioChannelsMatcher.matches(encoding.getAudioChannels())) {
				return false;
			}
			
			if (dogMatcher != null && ! dogMatcher.matches(encoding.getHasDOG())) {
				return false;
			}
			if (locationMatcher != null && ! locationMatcher.matches(encoding.getAvailableAt())) {
				return false;
			}
			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendValue("Encoding matching");
		}
		
	}
	
	static class LocationMatcher extends TypeSafeMatcher<Location> {

		private Matcher<String> transportTypeMatcher;
		private Matcher<String> transportSubTypeMatcher;
		private Matcher<String> uriMatcher;

		@Override
		public boolean matchesSafely(Location location) {
			if (transportTypeMatcher != null && !transportTypeMatcher.matches(location.getTransportType())) {
				return false;
			}
			if (transportSubTypeMatcher != null && !transportSubTypeMatcher.matches(location.getTransportSubType())) {
				return false;
			}
			if (uriMatcher != null && !uriMatcher.matches(location.getUri())) {
				return false;
			}
			return true;
		}

		public LocationMatcher withUri(Matcher<String> uriMatcher) {
			this.uriMatcher = uriMatcher;
			return this;
		}

		public LocationMatcher withTransportType(Matcher<String> transportTypeMatcher) {
			this.transportTypeMatcher = transportTypeMatcher;
			return this;
		}
		
		public LocationMatcher withTransportSubType(Matcher<String> transportSubTypeMatcher) {
			this.transportSubTypeMatcher = transportSubTypeMatcher;
			return this;
		}

		@Override
		public void describeTo(Description description) {
			description.appendValue("Location matching");
		}
	}
	
}
