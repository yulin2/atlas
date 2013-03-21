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

package org.atlasapi.remotesite.youtube;

import static org.atlasapi.remotesite.Matchers.encodingMatcher;
import static org.atlasapi.remotesite.Matchers.locationMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.youtube.YouTubeModel.VideoEntry;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.Duration;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.media.MimeType;

/**
 * Unit test for {@link YouTubeGraphExtractor}
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGraphExtractorTest extends TestCase {

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
		entry.title = "Video Title";
		source = new TestYouTubeSource(entry, ITEM_URI);
	}
	
	class TestYouTubeSource extends YouTubeSource {

		public TestYouTubeSource(VideoEntry entry, String episodeUri) {
			super(entry, episodeUri);
		}
		
		@Override
		List<Video> getVideos() {
			return Lists.newArrayList(new Video("application/x-shockwave-flash", Duration.standardMinutes(5), LOCATION_URI, 5, true), 
									  new Video("video/3gpp", Duration.standardMinutes(5), LOCATION_URI_2, 1, true),
					                  new Video("video/3gpp", Duration.standardMinutes(5), LOCATION_URI_3, 6, true));
		}
		
		@Override
		Set<String> getCategories() {
			return Sets.newHashSet("http://ref.atlasapi.org/genres/youtube/News");
		}
		
		@Override
		Set<String> getTags() {
			return Sets.newHashSet("http://ref.atlasapi.org/tags/funny");
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
		assertThat(item.getGenres(), is((Set<String>) Sets.<String>newHashSet("http://ref.atlasapi.org/genres/youtube/News", "http://ref.atlasapi.org/genres/atlas/news")));
		assertThat(item.getTags(), is((Set<String>) Sets.<String>newHashSet("http://ref.atlasapi.org/tags/funny")));
		assertThat(item.getPublisher(), is(Publisher.YOUTUBE));
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
			encodingMatcher()
				.withDataContainerFormat(is(MimeType.APPLICATION_XSHOCKWAVEFLASH))
				.withVideoCoding(is(not(MimeType.VIDEO_XVP6)))
				.withDOG(is(true))
				.withLocations(Matchers.<Location>hasItems(
						locationMatcher()
							.withTransportType(is(TransportType.EMBED))));
		
		
		Matcher<Encoding> encoding2 = 
			encodingMatcher()
				.withDataContainerFormat(is(MimeType.VIDEO_3GPP))
				.withVideoCoding(is(MimeType.VIDEO_H263))
				.withAudioCoding(is(MimeType.AUDIO_AMR))
				.withVideoHorizonalSize(is(176))
				.withVideoVerticalSize(is(144))
				.withAudioChannels(is(1))
				.withDOG(is(false))
				.withLocations(Matchers.<Location>hasItems(
						locationMatcher()
							.withTransportSubType(is(TransportSubType.RTSP))
							.withTransportType(is(TransportType.STREAM))));
		
		Matcher<Encoding> encoding3 = 
			encodingMatcher()
				.withDataContainerFormat(is(MimeType.VIDEO_3GPP))
				.withVideoCoding(is(MimeType.VIDEO_H263))
				.withAudioCoding(is(MimeType.AUDIO_MP4)) 
				.withVideoHorizonalSize(is(176)) 
				.withVideoVerticalSize(is(144)) 
				.withAudioChannels(is(1)) 
				.withDOG(is(false)) 
				.withLocations(Matchers.<Location>hasItems(
						locationMatcher()
							.withTransportSubType(is(TransportSubType.RTSP))
							.withTransportType(is(TransportType.STREAM))));
		
		Matcher<Encoding> encoding4 = 
			encodingMatcher()
				.withLocations(Matchers.<Location>hasItems(
						locationMatcher()
							.withUri(is(ITEM_URI))
							.withTransportType(is(TransportType.LINK))));
		
		assertThat(encodings, Matchers.<Encoding>hasItems(encoding1, encoding2, encoding3, encoding4));
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
		
		assertThat(location.getTransportType(), is(TransportType.LINK));
	}
	
}
