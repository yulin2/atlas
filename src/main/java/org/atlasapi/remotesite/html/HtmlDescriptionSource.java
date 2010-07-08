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

package org.atlasapi.remotesite.html;

import java.util.List;

import org.atlasapi.remotesite.BaseSource;

public class HtmlDescriptionSource extends BaseSource {

	private final HtmlDescriptionOfItem item;
	private String embedCode;

	public HtmlDescriptionSource(HtmlDescriptionOfItem item, String uri) {
		super(uri);
		this.item = item;
	}
	
	public HtmlDescriptionOfItem getItem() {
		return item;
	}

	public List<String> locationUris() {
		return item.getLocationUris();
	}

	public HtmlDescriptionSource withEmbedCode(String embedCode) {
		this.embedCode = embedCode;
		return this;
	}
	
	public String embedCode() {
		return embedCode;
	}
	
}
