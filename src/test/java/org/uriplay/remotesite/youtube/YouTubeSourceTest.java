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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.uriplay.remotesite.youtube.YouTubeSource.Video;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdata.data.Category;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;

public class YouTubeSourceTest extends TestCase {

	public void testReturnsNullForUninitalisedContent() throws Exception {
		
		YouTubeSource source = new YouTubeSource(new VideoEntry(), "uri");
		assertNull(source.getVideoTitle());
		assertNull(source.getDescription());
	}
	
	public void testGeneratesUriplayGenresAndTagsUrisForCategories() throws Exception {
		
		VideoEntry entry = new VideoEntry() { 
			@Override
			public YouTubeMediaGroup getMediaGroup() {
				return new YouTubeMediaGroup() {
					@Override
					public List<MediaCategory> getCategories() {
						return Lists.newArrayList(new MediaCategory("youtube.com", "News"));
					}
				};
			}
			
			@Override
			public Set<Category> getCategories() {
				return Sets.newHashSet(new Category("http://gdata.youtube.com/schemas/2007/categories.cat", "News"), 
						               new Category("http://gdata.youtube.com/schemas/2007/keywords.cat", "funny"));
			}
		};
				
		YouTubeSource source = new YouTubeSource(entry, "uri");
		assertThat(source.getCategories(), hasItem("http://uriplay.org/genres/youtube/News"));
		assertThat(source.getTags(), hasItem("http://uriplay.org/tags/funny"));
		assertThat(source.getTags(), not(hasItem("http://uriplay.org/tags/news")));
	}
	
	public void testStripsParametersFromLocationUris() throws Exception {
		
		Video video = new YouTubeSource.Video("type", 0, "http://www.youtube.com/v/pliAz4L-sAQ&f=videos&c=uriplay.org&app=youtube_gdata", 0, true);
		assertThat(video.getUrl(), is("http://www.youtube.com/v/pliAz4L-sAQ"));
	}
}
