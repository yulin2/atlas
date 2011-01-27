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

package org.atlasapi.remotesite.youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.youtube.YouTubeFeedClient.YouTubeUrl;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.json.JsonCParser;
import com.google.api.client.http.HttpTransport;

/**
 * Simple wrapper for Google's Java client for YouTube GData API
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGDataClient implements RemoteSiteClient<YouTubeFeedClient.VideoEntry> {

    public YouTubeFeedClient.VideoEntry get(String uri) throws Exception {
        HttpTransport transport = GoogleTransport.create();
        GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
        headers.setApplicationName("atlasapi.org");
        headers.gdataVersion = "2";
        transport.addParser(new JsonCParser());
        
        YouTubeUrl url = new YouTubeUrl("http://gdata.youtube.com/feeds/api/videos/" + videoIdFrom(uri));
        return YouTubeFeedClient.VideoEntry.executeGet(transport, url);
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
