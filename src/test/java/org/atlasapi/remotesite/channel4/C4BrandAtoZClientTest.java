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

package org.atlasapi.remotesite.channel4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.atlasapi.remotesite.FixedResponseHttpClient;
import org.atlasapi.remotesite.channel4.BrandListingPage;
import org.atlasapi.remotesite.channel4.C4BrandAtoZClient;
import org.atlasapi.remotesite.channel4.HtmlBrandSummary;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.io.Resources;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4BrandAtoZClientTest extends MockObjectTestCase {

	private String URI = "/uri";
	
	public void testTheClient() throws Exception {
		
		C4BrandAtoZClient client = new C4BrandAtoZClient(FixedResponseHttpClient.respondTo(URI, Resources.getResource("4od-atoz-a.html")));
		
		BrandListingPage page = client.get(URI);
		List<HtmlBrandSummary> brandList = page.getBrandList();
		
		assertThat(brandList.size(), is(17));
		
		HtmlBrandSummary firstBrand = brandList.get(0);
		assertThat(firstBrand.getTitle(), is("A Boy Called Alex: The Concert"));
		assertThat(firstBrand.getId(), is("a-boy-called-alex-the-concert"));
		assertThat(firstBrand.getBrandPage(), is("http://www.channel4.com/programmes/a-boy-called-alex-the-concert/4od"));
		assertThat(firstBrand.getImageUrl(), is("http://www.channel4.com/assets/programmes/images/a-boy-called-alex-the-concert/1f51d223-ff1c-447e-8020-059acb99f6cd_145x82.jpg"));
		assertThat(firstBrand.getCategories(), hasItem("http://www.channel4.com/programmes/tags/music"));

		assertTrue(page.hasNextPageLink());
		assertThat(page.getNextPageLink(), is("http://www.channel4.com/programmes/atoz/a/page-2"));
	}
}
