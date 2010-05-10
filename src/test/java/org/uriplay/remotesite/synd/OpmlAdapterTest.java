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

package org.uriplay.remotesite.synd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.ContentExtractor;

import com.sun.syndication.feed.opml.Opml;

/**
 * Unit test for {@link OpmlAdapter}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OpmlAdapterTest extends MockObjectTestCase {

	static final String OPML_LINK = "http://www.bbc.co.uk/radio/opml/bbc_podcast_opml.xml";

	static final String DOCUMENT = "doc";
	
	RemoteSiteClient<Opml> feedClient;
	ContentExtractor<OpmlSource, Playlist> propertyExtractor;
	OpmlAdapter adapter;
	Opml opml = null;
	RequestTimer timer = mock(RequestTimer.class);
	OpmlSource opmlSource = new OpmlSource(opml, OPML_LINK, timer);
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		feedClient = mock(RemoteSiteClient.class);
		propertyExtractor = mock(ContentExtractor.class);
		adapter = new OpmlAdapter(feedClient, propertyExtractor);
		
		checking(new Expectations() {{
			ignoring(timer);
		}});
	}
	
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		checking(new Expectations() {{
			one(feedClient).get(OPML_LINK); will(returnValue(opml));
			one(propertyExtractor).extract(opmlSource); will(returnValue(new Playlist()));
		}});
		
		adapter.fetch(OPML_LINK, timer);
	}
	
	public void testWrapsExceptionIfRemoteClientThrowsException() throws Exception {
		
		checking(new Expectations() {{
			allowing(feedClient).get(OPML_LINK); will(throwException(new IOException()));
		}});
		
		try {
			adapter.fetch(OPML_LINK, timer);
			
			fail("Should have thrown FetchException.");
		} catch (Exception e) {
			assertThat(e, instanceOf(FetchException.class));
		}
	}
	
	public void testCanFetchResourcesForOpmlUris() throws Exception {
		
		assertTrue(adapter.canFetch(OPML_LINK));
		assertTrue(adapter.canFetch("http://www.bbc.co.uk/radio/opml/bbc_podcast.opml"));
		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
		assertFalse(adapter.canFetch("http://www.youtube.com"));
	}
	
}
