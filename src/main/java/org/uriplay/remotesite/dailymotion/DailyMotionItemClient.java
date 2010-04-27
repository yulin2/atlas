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


package org.uriplay.remotesite.dailymotion;

import java.io.Reader;

import org.jherd.remotesite.http.CommonsHttpClient;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlNavigator;

public class DailyMotionItemClient implements RemoteSiteClient<HtmlDescriptionOfItem>  {

	private final RemoteSiteClient<Reader> client;

	public DailyMotionItemClient(RemoteSiteClient<Reader> client) {
		this.client = client;
	}

	public DailyMotionItemClient() {
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
		return item;
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
