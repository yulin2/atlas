package org.atlasapi.remotesite.youtube;

import org.atlasapi.remotesite.youtube.entity.YouTubeVideoFeed;


public class YouTubeFeedSource {

    private final YouTubeVideoFeed videoFeed;
    private final String uri;

    public YouTubeFeedSource(YouTubeVideoFeed videoFeed, String uri) {
        this.videoFeed = videoFeed;
        this.uri = uri;
    }
    
    public YouTubeVideoFeed getVideoFeed() {
        return videoFeed;
    }
    
    public String getUri() {
        return uri;
    }
}
