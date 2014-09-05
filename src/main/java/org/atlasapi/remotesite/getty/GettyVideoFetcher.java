package org.atlasapi.remotesite.getty;

import static org.atlasapi.remotesite.getty.VideoRequestSerializer.toJson;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.io.CharStreams;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.metabroadcast.common.media.MimeType;

public class GettyVideoFetcher {
    
    private static final String GETTY_URL = "https://connect.gettyimages.com/v1/search/SearchForVideos";
    
    private final int itemsPerPage;
    
    public GettyVideoFetcher(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
    
    public String getResponse(String token, String searchPhrase, int itemStartNumber) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(GETTY_URL);
        post.setHeader(HttpHeaders.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString());
        
        //maximum valid value for itemCount is 90 ; first itemStartNumber is 1
        VideoRequest videoRequest = new VideoRequest(token, searchPhrase, itemsPerPage, itemStartNumber);
        JsonElement json = toJson(videoRequest);
        post.setEntity(new StringEntity(json.toString()));
        
        HttpResponse resp = new DefaultHttpClient().execute(post);
        InputStreamReader reader = new InputStreamReader(resp.getEntity().getContent());
        return CharStreams.toString(reader);
    }
    
}
