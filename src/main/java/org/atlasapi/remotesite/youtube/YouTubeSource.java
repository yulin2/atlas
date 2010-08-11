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


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import org.atlasapi.feeds.Defect;
import org.atlasapi.remotesite.BaseSource;
import org.joda.time.Duration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdata.data.Category;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaContent;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.util.common.base.Charsets;

/**
 * Object that wraps data fetched from YouTube
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeSource extends BaseSource {
	
	private static final String ATLAS_GENRES_SCHEME = "http://ref.atlasapi.org/genres/youtube/";
	private static final String ATLAS_TAGS_SCHEME = "http://ref.atlasapi.org/tags/";
	private static final String GDATA_KEYWORDS_CAT_SCHEME = "http://gdata.youtube.com/schemas/2007/keywords.cat";
	
	private final VideoEntry videoEntry;
	
	public YouTubeSource(VideoEntry entry, String uri) {
		super(uri);
		videoEntry = entry;
	}
		
	String getVideoTitle() {
		if (videoEntry.getTitle() != null) {
			return videoEntry.getTitle().getPlainText();
		} else {
			return null;
		}
	}
	
	String getDescription() {
		if (videoEntry.getMediaGroup() != null && videoEntry.getMediaGroup().getDescription() != null) {
			return videoEntry.getMediaGroup().getDescription().getPlainTextContent();
		} else {
			return null;
		}
	}
	
	List<Video> getVideos() {
		List<Video> result = Lists.newArrayList();
		List<MediaContent> contents = videoEntry.getMediaGroup().getContents();
		for (MediaContent mediaContent : contents) {
			result.add(new Video(mediaContent, videoEntry.isEmbeddable()));
		}
		return result;
	}
	
	static class Video {

		private final String url;
		private final Duration duration;
		private final String type;
		private final int youtubeFormat;
		private final boolean embeddable;

		public Video(MediaContent mediaContent, boolean embeddable) {
			this(mediaContent.getType(), Duration.standardSeconds(mediaContent.getDuration()), mediaContent.getUrl(), ((YouTubeMediaContent)mediaContent).getYouTubeFormat(), embeddable);
		}

		public Video(String type, Duration duration, String locationUri, int youtubeFormat, boolean embeddable) {
			this.type = type;
			this.duration = duration;
			this.embeddable = embeddable;
			if (locationUri.contains("&")) {
				url = locationUri.substring(0, locationUri.indexOf('&'));
			} else {
				url = locationUri;
			}
			this.youtubeFormat = youtubeFormat;
		}

		public String getUrl() {
			return url;
		}

		public String getType() {
			return type;
		}
		
		public Duration getDuration() {
			return duration;
		}
		
		public int getYoutubeFormat() {
			return youtubeFormat;
		}

		public boolean isEmbeddable() {
			return embeddable;
		}
		
	}

	Set<String> getCategories() {
		Set<String> result = Sets.newHashSet();
		List<MediaCategory> categories = videoEntry.getMediaGroup().getCategories();
		for (MediaCategory category : categories) {
			result.add(ATLAS_GENRES_SCHEME + category.getContent());
		}
		return result;
	}

	Set<String> getTags() {
		Set<String> result = Sets.newHashSet();
		Set<Category> categories = videoEntry.getCategories();
		for (Category category : categories) {
			if (category.getScheme().equals(GDATA_KEYWORDS_CAT_SCHEME)) {
				try {
					result.add(ATLAS_TAGS_SCHEME + URLEncoder.encode(category.getTerm().toLowerCase(), Charsets.UTF_8.name()));
				} catch (UnsupportedEncodingException e) {
					throw new Defect("UTF-8 not found");
				}
			}
		}
		return result;
	}

	public String getThumbnailImageUri() {
		if (videoEntry.getMediaGroup().getThumbnails().isEmpty()) {
			return null;
		}
		return videoEntry.getMediaGroup().getThumbnails().get(0).getUrl();
	}
	
	public String getImageUri() {
		List<MediaThumbnail> thumbnails = videoEntry.getMediaGroup().getThumbnails();
		if (thumbnails == null || thumbnails.isEmpty()) { return null; }
		return thumbnails.get(thumbnails.size() - 1).getUrl();
	}
}
