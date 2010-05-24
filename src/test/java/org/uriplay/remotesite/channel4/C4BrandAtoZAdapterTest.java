/* Copyright 2009 Meta Broadcast Ltd

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
import org.uriplay.remotesite.SiteSpecificAdapter;

import com.google.common.collect.Lists;
/**
 * Unit test for {@link C4BrandAtoZAdapter}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4BrandAtoZAdapterTest extends MockObjectTestCase {

	String uri = "http://www.channel4.com/programmes/atoz/a";
	String uri2 = "http://www.channel4.com/programmes/atoz/a/page-2";
	
	RemoteSiteClient<BrandListingPage> itemClient;
	SiteSpecificAdapter<Brand> propertyExtractor;
	C4BrandAtoZAdapter adapter;
	
	Brand brand101 = new Brand();
	Brand brand102 = new Brand();
	Brand brand103 = new Brand();
	
	List<HtmlBrandSummary> brandListPage1 = Lists.newArrayList(new HtmlBrandSummary().withId("101"), new HtmlBrandSummary().withId("202"));
	List<HtmlBrandSummary> brandListPage2 = Lists.newArrayList(new HtmlBrandSummary().withId("303"));
	
	BrandListingPage page = new BrandListingPage(brandListPage1);

	BrandListingPage page1 = new BrandListingPage(brandListPage1).withNextPageLink("/programmes/atoz/a/page-2");
	BrandListingPage page2 = new BrandListingPage(brandListPage2);
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		itemClient = mock(RemoteSiteClient.class);
		propertyExtractor = mock(SiteSpecificAdapter.class);
		adapter = new C4BrandAtoZAdapter(itemClient, propertyExtractor);
	}
	
	public void testPerformsGetCorrespondingGivenUriAndPassesResultToExtractor() throws Exception {
		
		checking(new Expectations() {{
			one(itemClient).get(uri); will(returnValue(page));
			one(propertyExtractor).fetch("http://www.channel4.com/programmes/101/4od", null); will(returnValue(brand101));
			one(propertyExtractor).fetch("http://www.channel4.com/programmes/202/4od", null); will(returnValue(brand102));
		}});
		
		adapter.fetch(uri, null);
	}
	
	public void testWillFetchSubsequentPaginatedPages() throws Exception {
		
		checking(new Expectations() {{
			one(itemClient).get(uri); will(returnValue(page1));
			one(itemClient).get(uri2); will(returnValue(page2));
			one(propertyExtractor).fetch("http://www.channel4.com/programmes/101/4od", null); will(returnValue(brand101));
			one(propertyExtractor).fetch("http://www.channel4.com/programmes/202/4od", null); will(returnValue(brand102));
			one(propertyExtractor).fetch("http://www.channel4.com/programmes/303/4od", null); will(returnValue(brand103));
		}});
		
		Playlist playlist = adapter.fetch(uri, null);
		
		assertThat(playlist.getCanonicalUri(), is("http://www.channel4.com/programmes/atoz/a"));
		assertThat(playlist.getPlaylists(), is(Arrays.<Playlist>asList(brand101, brand102, brand103)));
	}
	
	public void testCanFetchResourcesFor4ODAtoZPages() throws Exception {
		
		assertTrue(adapter.canFetch("http://www.channel4.com/programmes/atoz/a"));
		assertTrue(adapter.canFetch("http://www.channel4.com/programmes/atoz/0-9"));
		assertFalse(adapter.canFetch("http://www.channel4.com"));
		assertFalse(adapter.canFetch("http://blip.tv/file/2114874?utm_source=episodepg_random&utm_medium=episodepg_random"));
	}
}
