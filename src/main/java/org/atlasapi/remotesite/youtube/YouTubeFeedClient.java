package org.atlasapi.remotesite.youtube;

import java.net.URL;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoFeed;

public class YouTubeFeedClient implements RemoteSiteClient<VideoFeed> {

    @Override
    public VideoFeed get(String uri) throws Exception {
        YouTubeService service = new YouTubeService("atlasapi.org");
        return service.getFeed(new URL(uri), VideoFeed.class);
    }
}
