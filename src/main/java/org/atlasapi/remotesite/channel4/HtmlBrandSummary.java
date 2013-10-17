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

import java.util.Set;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class HtmlBrandSummary {

	private static final String SLASH_PROGS_URL = "http://www.channel4.com/programmes/";
	
	private String title;
	private String id;

	private String imageUrl;

	private Set<String> categories;

	public String getTitle() {
		return title;
	}

	public HtmlBrandSummary withId(String id) {
		this.id = id;
		return this;
	}
	
	public HtmlBrandSummary withTitle(String title) {
		this.title = title;
		return this;
	}

	public HtmlBrandSummary withImageUrl(String url) {
		this.imageUrl = url;
		return this;
	}
	
	public String getId() {
		return id;
	}

	public String getBrandPage() {
		return SLASH_PROGS_URL + id;
	}

	public String getImageUrl() {
		return "http://www.channel4.com" + imageUrl;
	}

	public Set<String> getCategories() {
		return categories;
	}

	public HtmlBrandSummary withCategoryLinks(Set<String> categories) {
		this.categories = categories;
		return this;
	}

}
