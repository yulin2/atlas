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
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.deserializers.DateTimeDeserializer;
import org.atlasapi.remotesite.deserializers.LocalDateDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubeAccessControlDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubeContentDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubePlayerDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubeThumbnailDeserializer;
import org.atlasapi.remotesite.youtube.entity.YouTubeAccessControl;
import org.atlasapi.remotesite.youtube.entity.YouTubeContent;
import org.atlasapi.remotesite.youtube.entity.YouTubePlayer;
import org.atlasapi.remotesite.youtube.entity.YouTubeThumbnail;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoWrapper;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * Simple wrapper for Google's Java client for YouTube GData API
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeGDataClient implements RemoteSiteClient<YouTubeVideoEntry> {
    
    private final SimpleHttpClient client;
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
            .registerTypeAdapter(YouTubeContent.class, new YouTubeContentDeserializer())
            .registerTypeAdapter(YouTubeThumbnail.class, new YouTubeThumbnailDeserializer())
            .registerTypeAdapter(YouTubePlayer.class, new YouTubePlayerDeserializer())
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer(ISODateTimeFormat.dateTime()))
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer(ISODateTimeFormat.date()))
            .registerTypeAdapter(YouTubeAccessControl.class, new YouTubeAccessControlDeserializer())
            .create();
    
    public YouTubeGDataClient() {
        this(HttpClients.webserviceClient());
    }

    public YouTubeGDataClient(SimpleHttpClient client) {
        this.client = client;
    }

    public YouTubeVideoEntry get(String uri) throws Exception {
        String url = "http://gdata.youtube.com/feeds/api/videos/" + videoIdFrom(uri) + "?v=2&alt=jsonc";
        HttpResponse httpResponse = client.get(url);
        if (httpResponse.statusCode() >= 300) {
            throw new HttpStatusCodeException(httpResponse.statusCode(), httpResponse.statusLine()); 
        }
        YouTubeVideoWrapper wrapper = gson.fromJson(httpResponse.body(), YouTubeVideoWrapper.class);
        if (wrapper != null && wrapper.getData() != null) {
            return wrapper.getData();
        }
        return null;
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
