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
import org.atlasapi.remotesite.youtube.YouTubeModel.Content;
import org.atlasapi.remotesite.youtube.YouTubeModel.Player;
import org.atlasapi.remotesite.youtube.YouTubeModel.Thumbnail;
import org.atlasapi.remotesite.youtube.YouTubeModel.VideoEntry;
import org.atlasapi.remotesite.youtube.YouTubeModel.VideoWrapper;

import com.google.gson.FieldNamingPolicy;
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
public class YouTubeGDataClient implements RemoteSiteClient<VideoEntry> {
    
    private final SimpleHttpClient client;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Content.class, new YouTubeModel.ContentDeserializer())
            .registerTypeAdapter(Thumbnail.class, new YouTubeModel.ThumbnailDeserializer())
            .registerTypeAdapter(Player.class, new YouTubeModel.PlayerDeserializer())
            .create();
    
    public YouTubeGDataClient() {
        this(HttpClients.webserviceClient());
    }

    public YouTubeGDataClient(SimpleHttpClient client) {
        this.client = client;
    }

    public VideoEntry get(String uri) throws Exception {
        String url = "http://gdata.youtube.com/feeds/api/videos/" + videoIdFrom(uri) + "?v=2&alt=jsonc";
        HttpResponse httpResponse = client.get(url);
        if (httpResponse.statusCode() >= 300) {
            throw new HttpStatusCodeException(httpResponse); 
        }
        VideoWrapper wrapper = gson.fromJson(httpResponse.body(), VideoWrapper.class);
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
