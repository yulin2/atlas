package org.atlasapi.remotesite.youtube.user;

import com.google.gdata.data.youtube.PlaylistLinkFeed;

public class YouTubeUserSource {

    private final PlaylistLinkFeed videoFeed;
    private final String uri;

    public YouTubeUserSource(PlaylistLinkFeed videoFeed, String uri) {
        this.videoFeed = videoFeed;
        this.uri = uri;
    }
    
    public PlaylistLinkFeed getPlaylistFeed() {
        return videoFeed;
    }
    
    public String getUri() {
        return uri;
    }
}
