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

import java.util.List;

/**
 * Object representing data from pages like http://www.channel4.com/programmes/atoz/a
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BrandListingPage {

	private final List<HtmlBrandSummary> brandList;
	private boolean hasNextPageLink = false;
	private String nextPageLink;

	public BrandListingPage(List<HtmlBrandSummary> brandList) {
		this.brandList = brandList;
	}
	
	public List<HtmlBrandSummary> getBrandList() {
		return brandList;
	}

	public boolean hasNextPageLink() {
		return hasNextPageLink;
	}

	public BrandListingPage withNextPageLink(String uri) {
		if (uri != null) {
			hasNextPageLink = true;
			this.nextPageLink = uri;
		}
		return this;
	}

	public String getNextPageLink() {
		return "http://www.channel4.com" + nextPageLink;
	}

}
