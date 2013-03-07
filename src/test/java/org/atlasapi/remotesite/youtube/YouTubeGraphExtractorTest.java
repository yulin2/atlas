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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.youtube.entity.YouTubeSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeThumbnail;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

	static final String THUMBNAIL_URI = "http://i.ytimg.com/vi/XK7ZpW8Dq7E/default.jpg";
	static final String IMAGE_URI = "http://i.ytimg.com/vi/XK7ZpW8Dq7E/hqdefault.jpg";
	
	YouTubeGraphExtractor extractor = new YouTubeGraphExtractor();
	YouTubeVideoEntry entry = new YouTubeVideoEntry();
	YouTubeSource source;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		entry.setTitle("Video Title");
		entry.setCategory("news");
		YouTubeThumbnail thumb = new YouTubeThumbnail();
		thumb.setDefaultUrl(THUMBNAIL_URI);
		thumb.setHqDefault(IMAGE_URI);
		entry.setThumbnail(thumb);
		source = new TestYouTubeSource(entry, ITEM_URI);
	}
	
	class TestYouTubeSource extends YouTubeSource {

		public TestYouTubeSource(YouTubeVideoEntry entry, String episodeUri) {
			super(entry, episodeUri);
		}
		
		@Override
		public List<Video> getVideos() {
			return Lists.newArrayList(new Video("application/x-shockwave-flash", Duration.standardMinutes(5), LOCATION_URI, 5, true, entry.getUploaded()), 
									  new Video("video/3gpp", Duration.standardMinutes(5), LOCATION_URI_2, 1, true, entry.getUploaded()),
					                  new Video("video/3gpp", Duration.standardMinutes(5), LOCATION_URI_3, 6, true, entry.getUploaded()));
		}

		@Override
		public String getDescription() {
			return "Description of video";
		}
		
	}
	
	public void testCanExtractVideoTitleDescriptionCategories() throws Exception {
		Item item = extractor.extract(source);
		assertThat(item.getCanonicalUri(), is(ITEM_URI));
		assertThat(item.getTitle(), is("Video Title"));
		assertThat(item.getDescription(), is("Description of video"));
		assertThat(item.getGenres(), is((Set<String>) Sets.<String>newHashSet("http://www.youtube.com/news")));
		assertThat(item.getPublisher(), is(Publisher.YOUTUBE));
		assertThat(item.getThumbnail(),  is(THUMBNAIL_URI));
		assertThat(item.getImage(), is(IMAGE_URI));
		assertThat(item.getCurie(),  is("yt:otA7tjinFX4"));
	}
	
	class NoVideosYouTubeSource extends YouTubeSource {

		public NoVideosYouTubeSource(YouTubeVideoEntry entry, String episodeUri) {
			super(entry, episodeUri);
		}
		
		@Override
		public List<Video> getVideos() {
			return Lists.newArrayList();
		}
		
		@Override
		public Set<String> getCategories() {
			return Sets.newHashSet();
		}
		
		@Override
		public String getDescription() {
			return "Description of video";
		}
		
		@Override
		public String getThumbnailImageUri() {
			return THUMBNAIL_URI;
		}
		
		@Override
		public Optional<String> getImageUri() {
			return Optional.<String>of(IMAGE_URI);
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
