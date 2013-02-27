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

package org.atlasapi.remotesite.youtube.entity;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import org.atlasapi.remotesite.BaseSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Object that wraps data fetched from YouTube
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeSource extends BaseSource {
	
	// Genres = You tube categories.
    private static final String YOUTUBE_GENRES_PREFIX = "http://www.youtube.com/";

	private final YouTubeVideoEntry videoEntry;
	
	public YouTubeSource(YouTubeVideoEntry entry, String uri) {
		super(uri);
		checkNotNull(entry);
		videoEntry = entry;
	}
		
	public String getVideoTitle() {
		return videoEntry.getTitle();
	}
	
	public String getDescription() {
		return videoEntry.getDescription();
	}
    
    public Optional<String> getDefaultPlayerUrl(){
        if(videoEntry.getPlayer() != null)
            return Optional.<String>of(videoEntry.getPlayer().getDefaultUrl());
        
        return Optional.<String>absent();
    }
    
    public Optional<String> getMobilePlayerUrl(){
        if(videoEntry.getPlayer() != null)
            return Optional.<String>of(videoEntry.getPlayer().getMobileUrl());
        
        return Optional.<String>absent();
    }
	
	public Optional<String> getURL(){
	    if(videoEntry.getPlayer() != null)
	        return Optional.<String>of(videoEntry.getPlayer().getDefaultUrl());

	    return Optional.<String>absent();
	}

    public DateTime getUploaded(){
        return videoEntry.getUploaded();
    }

    public LocalDate getRecorded(){
        return videoEntry.getRecorded();
    }
	
	public List<Video> getVideos() {
		List<Video> result = Lists.newArrayList();
		if (videoEntry != null && videoEntry.getPlayer() != null && videoEntry.getPlayer().getDefaultUrl() != null) {
    		Video video = new Video("application/x-shockwave-flash", Duration.standardSeconds(videoEntry.getDuration()), videoEntry.getPlayer().getDefaultUrl(), topContent(), true, videoEntry.getUploaded());
    		result.add(video);
		}
		return result;
	}
	
	public int topContent() {
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

    public Optional<String>  getOne(){
        if(videoEntry.getContent() != null)
            return Optional.<String>of(videoEntry.getContent().getOne());
        
        return Optional.<String>absent();
    }

    public Optional<String> getSix(){
        if(videoEntry.getContent() != null)
            return Optional.<String>of(videoEntry.getContent().getSix());

        return Optional.<String>absent();
    }

    public Optional<String> getFive(){
        if(videoEntry.getContent() != null)
            return Optional.<String>of(videoEntry.getContent().getFive());

        return Optional.<String>absent();
    }

	public static class Video {

		private final String url;
		private final Duration duration;
		private final String type;
		private final int youtubeFormat;
        private final boolean embeddable;
        private final DateTime uploaded;

		public Video(String type, Duration duration, String locationUri, int youtubeFormat, boolean embeddable, DateTime uploaded) {
			this.type = type;
			this.duration = duration;
			this.embeddable = embeddable;
			if (locationUri.contains("&")) {
				url = locationUri.substring(0, locationUri.indexOf('&'));
			} else {
				url = locationUri;
			}
			this.youtubeFormat = youtubeFormat;
			this.uploaded = uploaded;
		}

		public DateTime getUploaded(){
		    return this.uploaded;
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

	public Set<String> getCategories() {
        Set<String> result = Sets.newHashSet();
        if (videoEntry != null && videoEntry.getCategory() != null) {
            try {
                String category = URLEncoder.encode(videoEntry.getCategory(), com.google.common.base.Charsets.UTF_8.name());
                if(category != null){
                    result.add(YOUTUBE_GENRES_PREFIX + category.toLowerCase());
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not found");
            }
        }
        return result;
    }

	public Set<String> getGenres() {
        return getCategories();
    }

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
	
	public Optional<String> getImageUri() {
	    if (videoEntry.getThumbnail() != null && videoEntry.getThumbnail().getHqDefault() != null) 
            return Optional.<String>of(videoEntry.getThumbnail().getHqDefault());

	    return Optional.<String>absent();
	}
}


