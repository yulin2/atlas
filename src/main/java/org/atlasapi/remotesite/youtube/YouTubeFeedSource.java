package org.atlasapi.remotesite.youtube;

import org.atlasapi.remotesite.youtube.YouTubeModel.VideoFeed;


public class YouTubeFeedSource {

    private final VideoFeed videoFeed;
    private final String uri;

    public YouTubeFeedSource(VideoFeed videoFeed, String uri) {
        this.videoFeed = videoFeed;
        this.uri = uri;
    }
    
    public VideoFeed getVideoFeed() {
        return videoFeed;
    }
    
    public String getUri() {
        return uri;
    }
}
