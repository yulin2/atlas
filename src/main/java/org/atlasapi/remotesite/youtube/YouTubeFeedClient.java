package org.atlasapi.remotesite.youtube;

import java.util.Map;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.youtube.YouTubeModel.Content;
import org.atlasapi.remotesite.youtube.YouTubeModel.FeedWrapper;
import org.atlasapi.remotesite.youtube.YouTubeModel.Player;
import org.atlasapi.remotesite.youtube.YouTubeModel.Thumbnail;
import org.atlasapi.remotesite.youtube.YouTubeModel.VideoFeed;

import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.url.Urls;

public class YouTubeFeedClient implements RemoteSiteClient<VideoFeed> {
    
    private static final Map<String, String> FETCH_PARAMETERS = ImmutableMap.of("v", "2", "alt", "jsonc");

    private final SimpleHttpClient client;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Content.class, new YouTubeModel.ContentDeserializer())
            .registerTypeAdapter(Thumbnail.class, new YouTubeModel.ThumbnailDeserializer())
            .registerTypeAdapter(Player.class, new YouTubeModel.PlayerDeserializer())
            .create();
    
    public YouTubeFeedClient() {
        this(HttpClients.webserviceClient());
    }

    public YouTubeFeedClient(SimpleHttpClient client) {
        this.client = client;
    }

    public VideoFeed get(String uri) throws Exception {
        HttpResponse httpResponse = client.get(Urls.appendParameters(uri, FETCH_PARAMETERS));
        if (httpResponse.statusCode() >= 300) {
            throw new HttpStatusCodeException(httpResponse.statusCode(), httpResponse.statusLine()); 
        }
        FeedWrapper wrapper = gson.fromJson(httpResponse.body(), FeedWrapper.class);
        if (wrapper != null && wrapper.getData() != null) {
            return wrapper.getData();
        }
        return null;
    }
}
