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

import org.atlasapi.remotesite.BaseSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.joda.time.Duration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Object that wraps data fetched from YouTube
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeSource extends BaseSource {
	
	private static final String ATLAS_GENRES_SCHEME = "http://ref.atlasapi.org/genres/youtube/";
	
	private final YouTubeVideoEntry videoEntry;
	
	public YouTubeSource(YouTubeVideoEntry entry, String uri) {
		super(uri);
		videoEntry = entry;
	}
		
	String getVideoTitle() {
		if (videoEntry.getTitle() != null) {
			return videoEntry.getTitle();
		} else {
			return null;
		}
	}
	
	String getDescription() {
		if (videoEntry.getDescription() != null) {
			return videoEntry.getDescription();
		} else {
			return null;
		}
	}
	
	List<Video> getVideos() {
		List<Video> result = Lists.newArrayList();
		if (videoEntry != null && videoEntry.getPlayer() != null && videoEntry.getPlayer().getDefaultUrl() != null) {
    		Video video = new Video("application/x-shockwave-flash", Duration.standardSeconds(videoEntry.getDuration()), videoEntry.getPlayer().getDefaultUrl(), topContent(), true);
    		result.add(video);
		}
		return result;
	}
	
	int topContent() {
	    if (videoEntry.getContent() != null) {
	        if (videoEntry.getContent().getSix() != null) {
	            return 6;
	        } else if (videoEntry.getContent().getOne() != null) {
	            return 1;
	        } else if (videoEntry.getContent().getFive() != null) {
	            return 5;
	        }
	    }
	    return 0;
	}
	
	static class Video {

		private final String url;
		private final Duration duration;
		private final String type;
		private final int youtubeFormat;
		private final boolean embeddable;

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
        if (videoEntry != null && videoEntry.getCategory() != null) {
            try {
                result.add(ATLAS_GENRES_SCHEME + URLEncoder.encode(videoEntry.getCategory(), com.google.common.base.Charsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not found");
            }
        }
        return result;
    }

//	Set<String> getTags() {
//		return ImmutableSet.copyOf(videoEntry.tags);
//	}

	public String getThumbnailImageUri() {
	    if (videoEntry.getThumbnail() != null) {
	        if (videoEntry.getThumbnail().getDefaultUrl() != null) {
	            return videoEntry.getThumbnail().getDefaultUrl();
	        } else if (videoEntry.getThumbnail().getSqDefault() != null) {
	            return videoEntry.getThumbnail().getSqDefault();
	        }
	    }
	    return null;
	}
	
	public String getImageUri() {
	    if (videoEntry.getThumbnail() != null && videoEntry.getThumbnail().getHqDefault() != null) {
            return videoEntry.getThumbnail().getHqDefault();
        }
	    return null;
	}
}
