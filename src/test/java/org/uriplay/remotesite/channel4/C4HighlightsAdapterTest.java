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

package org.uriplay.remotesite.channel4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.SiteSpecificAdapter;

import com.google.common.collect.Lists;

/**
 * Unit test for {@link C4HighlightsAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4HighlightsAdapterTest extends MockObjectTestCase {

	SiteSpecificAdapter<Brand> propertyExtractor;
	C4HighlightsAdapter adapter;
	RemoteSiteClient<BrandListingPage> c4client;
	
	Brand brand101 = new Brand();
	
	List<HtmlBrandSummary> listOfBrandSummaries = Lists.newArrayList(new HtmlBrandSummary().withId("101"));
	BrandListingPage brandListingPage = new BrandListingPage(listOfBrandSummaries);

	RequestTimer timer = mock(RequestTimer.class);
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		c4client = mock(RemoteSiteClient.class);
		propertyExtractor = mock(SiteSpecificAdapter.class);
		adapter = new C4HighlightsAdapter(c4client, propertyExtractor);
		
		checking(new Expectations() {{ 
			ignoring(timer);
		}});
	}
	
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		checking(new Expectations() {{
			one(c4client).get("http://www.channel4.com/programmes/4od/most-popular"); will(returnValue(brandListingPage));
			one(propertyExtractor).fetch("http://www.channel4.com/programmes/101/4od", timer); will(returnValue(brand101));
		}});
		
		Playlist playlist = adapter.fetch("http://www.channel4.com/programmes/4od/most-popular", timer);
		
		assertThat(playlist.getCanonicalUri(), is("http://www.channel4.com/programmes/4od/most-popular"));
		assertThat(playlist.getCurie(), is("c4:most-popular"));
		assertThat(playlist.getPlaylists(), is(Arrays.<Playlist>asList(brand101)));
	}
	
	public void testCanFetchResourcesForChannel4Brands() throws Exception {
		
		assertTrue(adapter.canFetch("http://www.channel4.com/programmes/4od/most-popular"));
		assertTrue(adapter.canFetch("http://www.channel4.com/programmes/4od/highlights"));
		assertFalse(adapter.canFetch("http://www.channel4.com/services/catchup-availability/brands/desperate-housewives"));
		assertFalse(adapter.canFetch("http://www.channel4.com/services/catchup-availability/brands/"));
		assertFalse(adapter.canFetch("http://www.channel4.com/services/catchup-availability/brands"));
		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
	}
	
}
