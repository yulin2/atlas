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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.atlasapi.remotesite.youtube.entity.YouTubeSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeSource.Video;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.joda.time.DateTime;
import org.joda.time.Duration;

public class YouTubeSourceTest extends TestCase {

	public void testReturnsNullForUninitalisedContent() throws Exception {
		
		YouTubeSource source = new YouTubeSource(new YouTubeVideoEntry(), "uri");
		assertNull(source.getVideoTitle());
		assertNull(source.getDescription());
	}
	
	public void testGeneratesGenresAndTagsUrisForCategories() throws Exception {
		
	    YouTubeVideoEntry entry = new YouTubeVideoEntry();
	    entry.setCategory("News");
				
		YouTubeSource source = new YouTubeSource(entry, "uri");
		assertThat(source.getCategories(), hasItem("http://www.youtube.com/news"));
	}
	
	public void testStripsParametersFromLocationUris() throws Exception {
		
		Video video = new YouTubeSource.Video("type", Duration.ZERO, "http://www.youtube.com/v/pliAz4L-sAQ&f=videos&c=ref.atlasapi.org&app=youtube_gdata", 0, true, new DateTime());
		assertThat(video.getUrl(), is("http://www.youtube.com/v/pliAz4L-sAQ"));
	}
}
