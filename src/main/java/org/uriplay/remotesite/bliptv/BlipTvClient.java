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


package org.uriplay.remotesite.bliptv;

import java.io.Reader;
import java.util.List;

import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlNavigator;
import org.uriplay.remotesite.http.CommonsHttpClient;

import com.google.common.collect.Lists;

public class BlipTvClient implements RemoteSiteClient<HtmlDescriptionOfItem>  {

	private final RemoteSiteClient<Reader> client;

	public BlipTvClient(RemoteSiteClient<Reader> client) {
		this.client = client;
	}

	public BlipTvClient() {
		this(new CommonsHttpClient());
	}

	public HtmlDescriptionOfItem get(String uri) throws Exception {
		Reader in = client.get(uri);
		
		HtmlNavigator html = new HtmlNavigator(in);
		HtmlDescriptionOfItem item = new HtmlDescriptionOfItem();

		item.setTitle(html.metaTagContents("title"));
		item.setDescription(html.metaTagContents("description"));
		item.parseKeywords(html.metaTagContents("keywords"));
		item.setThumbnail(html.linkTarget("image_src"));
		item.setVideoSource(videoSource(html));
		item.setLocationUris(locationUrisFrom(html));
		return item;
	}

	private List<String> locationUrisFrom(HtmlNavigator html) {
		List<String> relativeUrls = html.optionValuesWithinSelect("SelectFormat");
		List<String> absoluteUrls = Lists.newArrayListWithExpectedSize(relativeUrls.size());
		for (String url : relativeUrls) {
			absoluteUrls.add(applySubstitutionsTo("http://blip.tv" + url));
		}
		return absoluteUrls;
	}

	private String applySubstitutionsTo(String locationUri) {
		return locationUri.replaceAll("\\d+\\?filename=", "get/");
	}

	private String videoSource(HtmlNavigator html) {
		String src = html.linkTarget("video_src");
		if (src != null) {
			return src.replace("?autoPlay=1", "");
		} else {
			return null;
		}
	}
}
