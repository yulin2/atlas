package org.atlasapi.remotesite.youtube.user;

import java.net.URL;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.PlaylistFeed;

public class YouTubePlaylistClient implements RemoteSiteClient<PlaylistFeed> {

    @Override
    public PlaylistFeed get(String uri) throws Exception {
        YouTubeService service = new YouTubeService("atlasapi.org");
        return service.getFeed(new URL(uri), PlaylistFeed.class);
    }
}
