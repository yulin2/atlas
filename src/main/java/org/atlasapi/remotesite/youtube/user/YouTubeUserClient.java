package org.atlasapi.remotesite.youtube.user;

import java.net.URL;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.PlaylistLinkFeed;

public class YouTubeUserClient implements RemoteSiteClient<PlaylistLinkFeed> {

    @Override
    public PlaylistLinkFeed get(String uri) throws Exception {
        YouTubeService service = new YouTubeService("atlasapi.org");
        return service.getFeed(new URL(uri), PlaylistLinkFeed.class);
    }
}
