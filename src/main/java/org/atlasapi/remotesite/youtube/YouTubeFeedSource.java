package org.atlasapi.remotesite.youtube;


public class YouTubeFeedSource {

    private final YouTubeFeedClient.VideoFeed videoFeed;
    private final String uri;

    public YouTubeFeedSource(YouTubeFeedClient.VideoFeed videoFeed, String uri) {
        this.videoFeed = videoFeed;
        this.uri = uri;
    }
    
    public YouTubeFeedClient.VideoFeed getVideoFeed() {
        return videoFeed;
    }
    
    public String getUri() {
        return uri;
    }
}
