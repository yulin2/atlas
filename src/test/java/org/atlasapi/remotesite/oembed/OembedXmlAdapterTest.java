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

package org.atlasapi.remotesite.oembed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Item;
import org.atlasapi.output.oembed.OembedItem;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link OembedXmlAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
@RunWith(JMock.class)
public class OembedXmlAdapterTest extends TestCase {

	static final String VIDEO_URI = "http://www.vimeo.com/757219";
	static final String OEMBED_ENDPOINT_URI = "http://www.vimeo.com/api/oembed.xml";

    private final Mockery context = new Mockery();
	RemoteSiteClient<OembedItem> oembedClient;
	ContentExtractor<OembedSource, Item> propertyExtractor;
	OembedXmlAdapter adapter;
	OembedItem oembed = new OembedItem();
	OembedSource oembedSource = new OembedSource(oembed, VIDEO_URI);

	
	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		oembedClient = context.mock(RemoteSiteClient.class);
		propertyExtractor = context.mock(ContentExtractor.class);
		adapter = new OembedXmlAdapter(oembedClient, propertyExtractor);
		adapter.setOembedEndpoint(OEMBED_ENDPOINT_URI);
		adapter.setAcceptedUriPattern("http://www.vimeo.com/\\d+");
	}
	
	@Test
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		context.checking(new Expectations() {{
			one(oembedClient).get(OEMBED_ENDPOINT_URI + "?url=" + VIDEO_URI); will(returnValue(oembed));
			one(propertyExtractor).extract(oembedSource); will(returnValue(new Item()));
		}});
		
		adapter.fetch(VIDEO_URI);
	}
	
	@Test
	public void testPassesMaxWidthParamIfSet() throws Exception {
		
		adapter.setMaxWidth(400);
		
		context.checking(new Expectations() {{
			one(oembedClient).get(OEMBED_ENDPOINT_URI + "?url=" + VIDEO_URI + "&maxwidth=400"); will(returnValue(oembed));
			ignoring(propertyExtractor);  will(returnValue(new Item()));
		}});
		
		adapter.fetch(VIDEO_URI);
	}
	
	public void testPassesMaxHeightParamIfSet() throws Exception {
		
		adapter.setMaxHeight(200);
		
		context.checking(new Expectations() {{
			one(oembedClient).get(OEMBED_ENDPOINT_URI + "?url=" + VIDEO_URI + "&maxheight=200"); will(returnValue(oembed));
			ignoring(propertyExtractor);  will(returnValue(new Item()));
		}});
		
		adapter.fetch(VIDEO_URI);
	}
	
	public void testWrapsExceptionIfClientThrowsJaxbException() throws Exception {
		
		context.checking(new Expectations() {{
			allowing(oembedClient).get(OEMBED_ENDPOINT_URI + "?url=" + VIDEO_URI); will(throwException(new JAXBException("dummy")));
		}});
		
		try {
			adapter.fetch(VIDEO_URI);
			fail("Should have thrown FetchException.");
		} catch (Exception e) {
			assertThat(e, instanceOf(FetchException.class));
		}
	}

	
	public void testCanFetchResourcesForConfiguredPattern() throws Exception {
		
		assertTrue(adapter.canFetch(VIDEO_URI));
		assertTrue(adapter.canFetch("http://www.vimeo.com/1234"));
		assertFalse(adapter.canFetch("http://www.channel4.com/services/catchup-availability/brands"));
		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
	}
	
}
