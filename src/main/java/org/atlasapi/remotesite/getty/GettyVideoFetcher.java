package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;

public class GettyVideoFetcher {

    private static final int ITEMS_PER_PAGE = 90;
    
    private final JsonVideoRequest jsonVideoRequest;
    
    public GettyVideoFetcher(JsonVideoRequest jsonVideoRequest) {
        this.jsonVideoRequest = checkNotNull(jsonVideoRequest);
    }
    
    public String getResponse(String token, String searchPhrase, int itemStartNumber) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost("https://connect.gettyimages.com/v1/search/SearchForVideos");
        post.setHeader("Content-type", "application/json");
        
        //maximum valid value for itemCount is 90 ; first itemStartNumber is 1
        VideoRequest videoRequest = new VideoRequest(token, searchPhrase, ITEMS_PER_PAGE, itemStartNumber);
        JsonElement json = jsonVideoRequest.toJson(videoRequest);
        post.setEntity(new StringEntity(json.toString()));
        
        HttpResponse resp = new DefaultHttpClient().execute(post);
        InputStreamReader reader = new InputStreamReader(resp.getEntity().getContent());
        return CharStreams.toString(reader);
    }
    
}
