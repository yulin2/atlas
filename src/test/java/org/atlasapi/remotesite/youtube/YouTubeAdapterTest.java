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
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.youtube.entity.YouTubeSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.metabroadcast.common.http.HttpStatusCodeException;

/**
 * Unit test for {@link YouTubeAdapter}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@RunWith(JMock.class)
public class YouTubeAdapterTest extends TestCase {

    private final Mockery context = new Mockery();
	static final String DOCUMENT = "doc";
	
	RemoteSiteClient<YouTubeVideoEntry> gdataClient;
	ContentExtractor<YouTubeSource, Item> contentExtractor;
	YouTubeAdapter adapter;
	YouTubeVideoEntry videoEntry = null;
	YouTubeSource youtubeSource = new YouTubeSource(videoEntry, "http://wwww.youtube.com/watch?v=-OBxL8PiFc8");

	
	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		gdataClient = context.mock(RemoteSiteClient.class);
		contentExtractor = context.mock(ContentExtractor.class);
		adapter = new YouTubeAdapter(gdataClient, contentExtractor);
	}
	
	@Test
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		context.checking(new Expectations() {{
			one(gdataClient).get("http://wwww.youtube.com/watch?v=-OBxL8PiFc8"); will(returnValue(videoEntry));
			one(contentExtractor).extract(youtubeSource); will(returnValue(new Item()));
		}});
		
		adapter.fetch("http://wwww.youtube.com/watch?v=-OBxL8PiFc8");
	}
	
	@Test
	public void testWrapsExceptionIfGDataClientThrowsIOException() throws Exception {
		
		context.checking(new Expectations() {{
			allowing(gdataClient).get("http://wwww.youtube.com/watch?v=-OBxL8PiFc8"); will(throwException(new IOException()));
		}});
		
		try {
			adapter.fetch("http://wwww.youtube.com/watch?v=-OBxL8PiFc8");
			fail("Should have thrown FetchException.");
		} catch (Exception e) {
			assertThat(e, instanceOf(FetchException.class));
		}
	}
	
	public void tesRetunsNullIfGDataClientThrowsResourceNotFoundException() throws Exception {
		
		context.checking(new Expectations() {{
			allowing(gdataClient).get("http://wwww.youtube.com/watch?v=-OBxL8PiFc8"); 
				will(throwException(new HttpStatusCodeException(404, "Not Found")));
		}});
		
		assertNull(adapter.fetch("http://wwww.youtube.com/watch?v=-OBxL8PiFc8"));
	}
	
	@Test
	public void testCanFetchResourcesForYouTubeClipUris() throws Exception {
		assertTrue(adapter.canFetch("http://www.youtube.com/watch?v=-OBxL8PiFc8"));
		
		// should be canonicalised before this request reaches here
		assertFalse(adapter.canFetch("http://www.youtube.com/watch?v=-OBxL8PiFc8&featured=true"));
		
		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
		assertFalse(adapter.canFetch("rtsp://rtsp2.youtube.com/Ci8LENy73wIaJgka3WEi7jmPGhMYDSANFEILdXJpcGxheS5vcmdIBlIGdmlkZW9zDA==/0/0/0/video.3gpf"));
	}
	
}
