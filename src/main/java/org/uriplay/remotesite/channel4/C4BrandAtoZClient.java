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

import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.HttpClients;
import org.uriplay.remotesite.html.HtmlNavigator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4BrandAtoZClient implements RemoteSiteClient<BrandListingPage> {

	private final SimpleHttpClient client;

	public C4BrandAtoZClient(SimpleHttpClient client) {
		this.client = client;
	}

	public C4BrandAtoZClient() {
		this(HttpClients.screenScrapingClient());
	}

	public BrandListingPage get(String uri) throws Exception {
		
		HtmlNavigator html = new HtmlNavigator(client.getContentsOf(uri));
		List<HtmlBrandSummary> brandList = Lists.newArrayList();
		
		List<Element> brandListItems = html.allElementsMatching(".//ul[@id='BrandResultList']/li");
		
		for (Element brand : brandListItems) {
			
			Element brandLinkElement = html.firstElementOrNull(".//a[@class='watch-on-medium']", brand);
			if (brandLinkElement == null) {
				continue; 
			}
			
			String brandUri = brandLinkElement.getAttributeValue("href");
			
			if (!brandUri.startsWith("/programmes/")) {
				continue;
			}

			String  brandId = brandUri.replace("/programmes/", "").replace("/4od", "");
			
			Element titleSpan = html.firstElementOrNull("./h3/a/span[@class='title']", brand);
			Element imgTag = html.firstElementOrNull("./h3/a/img", brand);

			String imageUrl = imgTag.getAttributeValue("src");
			
			List<Element> allCategoryLinks = html.allElementsMatching("./div[@class='categories']/ul/li/a", brand);
			Set<String> allCategoryUris = categoryUrisFrom(allCategoryLinks);
			
			brandList.add(new HtmlBrandSummary().withTitle(titleSpan.getText())
					                            .withId(brandId)
					                            .withImageUrl(imageUrl)
					                            .withCategoryLinks(allCategoryUris));
		}
		
		Element nextPageLink = html.firstElementOrNull("//a[.='Next page']");
		
		if (nextPageLink != null) {
			return new BrandListingPage(brandList).withNextPageLink(nextPageLink.getAttributeValue("href"));
		} else {
			return new BrandListingPage(brandList);
		}
	}

	private Set<String> categoryUrisFrom(List<Element> allCategoryLinks) {
		Set<String> uris = Sets.newLinkedHashSet();
		for (Element link : allCategoryLinks) {
			uris.add("http://www.channel4.com" + link.getAttributeValue("href"));
		}
		
		return uris;
	}


}
