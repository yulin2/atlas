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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.remotesite.FetchException;

import com.google.gdata.data.Link;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;

/**
 * Test of the behaviour of the third-party YouTube GData client from Google.
 * Exercised through our thin wrapper.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGdataClientTest extends TestCase {

	YouTubeGDataClient gdataClient = new YouTubeGDataClient();

	public void testCanRetrieveDataRelatingToGivenYouTubePage() throws Exception {

		VideoEntry entry = gdataClient.get("http://www.youtube.com/watch?v=pdyYe7sDlhA");
		
		assertThat(entry.getTitle().getPlainText(), containsString("BBC News 24"));
		assertThat(entry.getHtmlLink().getHref(), startsWith("http://www.youtube.com/watch?v=pdyYe7sDlhA&feature=youtube_gdata")); // should remove youtube_gdata flag
		MediaContent content = (MediaContent) entry.getContent();
		assertThat(content.getUri(), startsWith("http://www.youtube.com/v/pdyYe7sDlhA")); // but has more params 

		YouTubeMediaGroup mediaGroup = entry.getMediaGroup();
		assertThat(mediaGroup.getDescription().getPlainTextContent(), startsWith("On May 8th 2006"));
		List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
		assertThat(thumbnails.get(0).getUrl(), is("http://i.ytimg.com/vi/pdyYe7sDlhA/default.jpg"));
		assertThat(thumbnails.get(thumbnails.size() - 1).getUrl(), is("http://i.ytimg.com/vi/pdyYe7sDlhA/hqdefault.jpg"));
		List<com.google.gdata.data.media.mediarss.MediaContent> contents = mediaGroup.getContents();
		
		for (com.google.gdata.data.media.mediarss.MediaContent mediaContent : contents) {
			mediaContent.getType();
			mediaContent.getDuration();
			mediaContent.getUrl();
		}
	}
	
	public void testhrowsExceptionIfSubmittedUriDoesNotContainVideoId() throws Exception {
		
		try {
			gdataClient.get("http://uk.youtube.com/watch/blah");
		} catch (FetchException fe) {
			assertThat(fe.getMessage(), containsString("URI did not contain a recognised video id"));
		}
	}
	
	public void testTellsUsAboutDifferentVideoQualitysAvailalbe() throws Exception {
		
		VideoEntry entry = gdataClient.get("http://www.youtube.com/watch?v=ilHDZBb-hI0");
		
		YouTubeMediaGroup mediaGroup = entry.getMediaGroup();
		List<com.google.gdata.data.media.mediarss.MediaContent> contents = mediaGroup.getContents();
		
		for (com.google.gdata.data.media.mediarss.MediaContent mediaContent : contents) {
			mediaContent.getType();
			mediaContent.getDuration();
			System.out.println(mediaContent.getUrl());
		}
		
		List<Link> links = entry.getLinks();
		for (Link link : links) {
			System.out.println(link.getType() + " : " + link.getRel() + " : " +  link.getHref());
		}
	}
}
