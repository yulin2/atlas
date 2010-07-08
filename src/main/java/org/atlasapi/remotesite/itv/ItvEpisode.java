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

package org.atlasapi.remotesite.itv;

/**
 * Representation of data about a single ITV episode, gained by
 * scraping fragments of HTML from the itv.com website.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ItvEpisode {

	private final String url;
	private final String date;
	private final String description;

	public ItvEpisode(String date, String description, String episodePage) {
		this.date = date;
		this.description = description;
		this.url = episodePage;
	}

	public String url() {
		return url;
	}

	public String date() {
		return date;
	}
	
	public String description() {
		return description;
	}

}
