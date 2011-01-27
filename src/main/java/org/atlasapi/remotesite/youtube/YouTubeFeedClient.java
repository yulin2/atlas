package org.atlasapi.remotesite.youtube;

import java.io.IOException;
import java.util.List;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.json.JsonCParser;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;
import com.google.common.collect.Lists;

public class YouTubeFeedClient implements RemoteSiteClient<YouTubeFeedClient.VideoFeed> {

    @Override
    public YouTubeFeedClient.VideoFeed get(String uri) throws Exception {
        HttpTransport transport = GoogleTransport.create();
        GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
        headers.setApplicationName("atlasapi.org");
        headers.gdataVersion = "2";
        transport.addParser(new JsonCParser());
        
        YouTubeUrl url = new YouTubeUrl(uri);
        return VideoFeed.executeGet(transport, url);
    }
    
    public static class VideoFeed {
        @Key("items") public List<VideoEntry> videos;
        
        @Key public int itemsPerPage;
        @Key public int startIndex;
        @Key public int totalItems;

        static VideoFeed executeGet(HttpTransport transport, YouTubeUrl url) throws IOException {
            HttpRequest request = transport.buildGetRequest();
            request.url = url;
            return request.execute().parseAs(VideoFeed.class);
        }
    }

    public static class VideoEntry {
        @Key String id;
        @Key String title;
        @Key String description;
        @Key String category;
        @Key Player player;
        @Key List<String> tags = Lists.newArrayList();
        @Key Thumbnail thumbnail;
        @Key int duration;
        
        
        static VideoEntry executeGet(HttpTransport transport, YouTubeUrl url) throws IOException {
            HttpRequest request = transport.buildGetRequest();
            request.url = url;
            return request.execute().parseAs(VideoEntry.class);
        }
    }
    
    public static class Player {
        @Key("default") String defaultUrl;
    }
    
    public static class Thumbnail {
        @Key("default") String defaultUrl;
        @Key String sqDefault;
        @Key String hqDefault;
    }

    public static class YouTubeUrl extends GenericUrl {
        @Key final String alt = "jsonc";
        @Key final String v = "2";
        @Key String author;
        @Key("max-results") Integer maxResults;
        
        YouTubeUrl(String url) { super(url); }
    }
}
