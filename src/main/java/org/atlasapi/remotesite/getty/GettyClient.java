package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.getty.VideoRequestSerializer.toJson;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;

public class GettyClient {

    private static final String EXPIRED_TOKEN_CODE = "AUTH-012";
    
    private final GettyTokenFetcher tokenFetcher;
    private final int itemsPerPage;
    private final boolean quoteSearchPhrases;
    
    private String token;
    
    /**
     * @param quoteSearchPhrases Whether to wrap multi-word search strings in quotes (more precision, fewer results).
     */
    public GettyClient(GettyTokenFetcher tokenFetcher, int itemsPerPage, boolean quoteSearchPhrases) {
        this.tokenFetcher = checkNotNull(tokenFetcher);
        this.itemsPerPage = itemsPerPage;
        this.quoteSearchPhrases = quoteSearchPhrases;
    }
    
    public String getVideoResponse(String keyword, int offset) throws ClientProtocolException, IOException {
        this.token = tokenFetcher.getToken();
        String response = getResponse(token, keyword, offset);
        if (response.contains(EXPIRED_TOKEN_CODE)) {
            this.token = tokenFetcher.getToken();
            return getResponse(token, keyword, offset);
        }
        return response;
    }
    
    private String getResponse(String token, String searchPhrase, int itemStartNumber) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost("https://connect.gettyimages.com/v1/search/SearchForVideos");
        post.setHeader("Content-type", "application/json");

        if (quoteSearchPhrases && searchPhrase.contains(" ")) {
            searchPhrase = "\"" + searchPhrase + "\"";
        }

        //maximum valid value for itemCount is 90 ; first itemStartNumber is 1
        VideoRequest videoRequest = new VideoRequest(token, searchPhrase, itemsPerPage, itemStartNumber);
        JsonElement json = toJson(videoRequest);
        post.setEntity(new StringEntity(json.toString()));
        
        HttpResponse resp = new DefaultHttpClient().execute(post);
        InputStreamReader reader = new InputStreamReader(resp.getEntity().getContent());
        return CharStreams.toString(reader);
    }
    
}
