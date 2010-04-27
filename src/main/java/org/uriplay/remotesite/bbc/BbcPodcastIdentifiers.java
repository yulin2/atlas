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

package org.uriplay.remotesite.bbc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Generates ids for BBC podcast based on location uri.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
class BbcPodcastIdentifiers {

	private final String locationUri;
	private final String episodeUri;

	public BbcPodcastIdentifiers(SyndEntry entry) {
		this.locationUri = entry.getLink();
		this.episodeUri = extractEpisodeUri(locationUri);
	}

	String episodeUri() {
		return episodeUri;
	}

	String versionUri() {
		return episodeUri() + "/main";
	}
	
	String encodingUri() {
		return versionUri() + "/main";
	}

	public String locationUri() {
		return locationUri;
	}

	private String extractEpisodeUri(String uri) {
		Pattern regex = Pattern.compile("http://downloads.bbc.co.uk/podcasts/[^/]*/([^/]*/[^/]*).(mp3|m4a)");
		Matcher matcher = regex.matcher(uri);
		if (matcher.find()) {
			return "http://uriplay.org/" + matcher.group(1);
		} 
		
		return null;
	}

}
