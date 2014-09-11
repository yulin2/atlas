package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.security.UsernameAndPassword;

public class IrisKeywordsFetcher {

    private static final String TOP_NAME = "keywords";
    private static final String KEYWORD_TITLE = "label";
    
    private final SimpleHttpClient httpClient;
    private final String irisUrl;
    
    public IrisKeywordsFetcher(UsernameAndPassword credentials, String irisUrl) {
        this.irisUrl = checkNotNull(irisUrl);
        checkNotNull(credentials);
        this.httpClient = new SimpleHttpClientBuilder()
            .withAcceptHeader(MimeType.APPLICATION_JSON)
            .withPreemptiveBasicAuth(credentials)
            .build();
    }
    
    public List<String> getKeywordsFromOffset(int offset) throws HttpException, Exception {
        String response = getResponse(offset);
        return parseResponse(response);
    }
    
    private List<String> parseResponse(String response) {
        Builder<String> keywords = new ImmutableList.Builder<String>();
        JsonObject parse = (JsonObject) new JsonParser().parse(response);
        JsonArray resources = (JsonArray) parse.get(TOP_NAME);
        for (JsonElement elem : resources) {
            keywords.add(elem.getAsJsonObject().get(KEYWORD_TITLE).getAsString());
        }
        return keywords.build();
    }
    
    private String getResponse(int offset) throws HttpException, Exception {
        String irisUrlWithOffset = String.format("%s?offset=%s", irisUrl, offset);
        SimpleHttpRequest<String> request = new SimpleHttpRequest<String>(irisUrlWithOffset, new HttpResponseTransformer<String>() {

            @Override
            public String transform(HttpResponsePrologue prologue, InputStream body) throws Exception {
                InputStreamReader reader = new InputStreamReader(body);
                return CharStreams.toString(reader);
            }
        });
        return httpClient.get(request);
    }
    
}
