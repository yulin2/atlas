package org.atlasapi.remotesite.youtube;

import java.util.Map;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.deserializers.DateTimeDeserializer;
import org.atlasapi.remotesite.deserializers.LocalDateDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubeAccessControlDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubeContentDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubePlayerDeserializer;
import org.atlasapi.remotesite.youtube.deserializers.YouTubeThumbnailDeserializer;
import org.atlasapi.remotesite.youtube.entity.YouTubeAccessControl;
import org.atlasapi.remotesite.youtube.entity.YouTubeContent;
import org.atlasapi.remotesite.youtube.entity.YouTubeFeedWrapper;
import org.atlasapi.remotesite.youtube.entity.YouTubePlayer;
import org.atlasapi.remotesite.youtube.entity.YouTubeThumbnail;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoFeed;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.url.Urls;

public class YouTubeFeedClient implements RemoteSiteClient<YouTubeVideoFeed> {
    
    private static final Map<String, String> FETCH_PARAMETERS = ImmutableMap.of("v", "2", "alt", "jsonc");

    private final SimpleHttpClient client;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
            .registerTypeAdapter(YouTubeContent.class, new YouTubeContentDeserializer())
            .registerTypeAdapter(YouTubeThumbnail.class, new YouTubeThumbnailDeserializer())
            .registerTypeAdapter(YouTubePlayer.class, new YouTubePlayerDeserializer())
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .registerTypeAdapter(YouTubeAccessControl.class, new YouTubeAccessControlDeserializer())
            .create();
    
    public YouTubeFeedClient() {
        this(HttpClients.webserviceClient());
    }

    public YouTubeFeedClient(SimpleHttpClient client) {
        this.client = client;
    }

    public YouTubeVideoFeed get(String uri) throws Exception {
        HttpResponse httpResponse = client.get(Urls.appendParameters(uri, FETCH_PARAMETERS));
        if (httpResponse.statusCode() >= 300) {
            throw new HttpStatusCodeException(httpResponse.statusCode(), httpResponse.statusLine()); 
        }
        YouTubeFeedWrapper wrapper = gson.fromJson(httpResponse.body(), YouTubeFeedWrapper.class);
        if (wrapper != null && wrapper.getData() != null) {
            return wrapper.getData();
        }
        return null;
    }
}
