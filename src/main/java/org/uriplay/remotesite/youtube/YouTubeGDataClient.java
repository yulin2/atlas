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

package org.uriplay.remotesite.youtube;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.http.RemoteSiteClient;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;

/**
 * Simple wrapper for Google's Java client for YouTube GData API
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGDataClient implements RemoteSiteClient<VideoEntry> {

	public VideoEntry get(String uri) throws Exception {
		
		YouTubeService service = new YouTubeService("uriplay.org");
		
		URL entryUrl = new URL("http://gdata.youtube.com/feeds/api/videos/" + videoIdFrom(uri));

		return service.getEntry(entryUrl, VideoEntry.class);
	}
	
	private String videoIdFrom(String uri) {
		Pattern regex = Pattern.compile("v=([^&]*)");
		Matcher matcher = regex.matcher(uri);
		if (matcher.find()) {
			return matcher.group(1);
		} 
		throw new FetchException("URI did not contain a recognised video id: " + uri);
	}

}
