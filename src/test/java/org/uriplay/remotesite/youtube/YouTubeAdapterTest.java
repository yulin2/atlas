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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Item;
import org.uriplay.remotesite.ContentExtractor;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ResourceNotFoundException;

/**
 * Unit test for {@link YouTubeAdapter}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeAdapterTest extends MockObjectTestCase {

	static final String DOCUMENT = "doc";
	
	RemoteSiteClient<VideoEntry> gdataClient;
	ContentExtractor<YouTubeSource, Item> contentExtractor;
	YouTubeAdapter adapter;
	VideoEntry videoEntry = null;
	YouTubeSource youtubeSource = new YouTubeSource(videoEntry, "http://uk.youtube.com/watch?v=-OBxL8PiFc8");

	RequestTimer timer = mock(RequestTimer.class);
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		gdataClient = mock(RemoteSiteClient.class);
		contentExtractor = mock(ContentExtractor.class);
		adapter = new YouTubeAdapter(gdataClient, contentExtractor);
		
		checking(new Expectations() {{ 
			ignoring(timer);
		}});
	}
	
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		checking(new Expectations() {{
			one(gdataClient).get("http://uk.youtube.com/watch?v=-OBxL8PiFc8"); will(returnValue(videoEntry));
			one(contentExtractor).extract(youtubeSource); will(returnValue(new Item()));
		}});
		
		adapter.fetch("http://uk.youtube.com/watch?v=-OBxL8PiFc8", timer);
	}
	
	public void testWrapsExceptionIfGDataClientThrowsIOException() throws Exception {
		
		checking(new Expectations() {{
			allowing(gdataClient).get("http://uk.youtube.com/watch?v=-OBxL8PiFc8"); will(throwException(new IOException()));
		}});
		
		try {
			adapter.fetch("http://uk.youtube.com/watch?v=-OBxL8PiFc8", timer);
			fail("Should have thrown FetchException.");
		} catch (Exception e) {
			assertThat(e, instanceOf(FetchException.class));
		}
	}
	
	public void testWrapsExceptionIfGDataClientThrowsResourceNotFoundException() throws Exception {
		
		checking(new Expectations() {{
			allowing(gdataClient).get("http://uk.youtube.com/watch?v=-OBxL8PiFc8"); 
				will(throwException(new ResourceNotFoundException("Video not found")));
		}});
		
		try {
			adapter.fetch("http://uk.youtube.com/watch?v=-OBxL8PiFc8", timer);
			fail("Should have thrown FetchException.");
		} catch (FetchException fe) {
			assertThat(fe.getMessage(), is("Video not found on YouTube: http://uk.youtube.com/watch?v=-OBxL8PiFc8"));
		}
	}
	
	public void testCanFetchResourcesForYouTubeClipUris() throws Exception {
		assertTrue(adapter.canFetch("http://www.youtube.com/watch?v=-OBxL8PiFc8"));
		
		// should be canonicalised before this request reaches here
		assertFalse(adapter.canFetch("http://www.youtube.com/watch?v=-OBxL8PiFc8&featured=true"));
		
		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
		assertFalse(adapter.canFetch("rtsp://rtsp2.youtube.com/Ci8LENy73wIaJgka3WEi7jmPGhMYDSANFEILdXJpcGxheS5vcmdIBlIGdmlkZW9zDA==/0/0/0/video.3gpf"));
	}
	
}
