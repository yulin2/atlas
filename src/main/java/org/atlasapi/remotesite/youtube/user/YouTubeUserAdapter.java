package org.atlasapi.remotesite.youtube.user;

import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.google.gdata.data.youtube.PlaylistLinkFeed;

public class YouTubeUserAdapter implements SiteSpecificAdapter<Playlist> {
    
    private final RemoteSiteClient<PlaylistLinkFeed> gdataClient;
    private final ContentExtractor<YouTubeUserSource, Playlist> playlistExtractor;
    
    public YouTubeUserAdapter() {
        this(new YouTubeUserClient(), new YouTubeUserExtractor());
    }
    
    public YouTubeUserAdapter(RemoteSiteClient<PlaylistLinkFeed> gdataClient, ContentExtractor<YouTubeUserSource, Playlist> playlistExtractor) {
        this.gdataClient = gdataClient;
        this.playlistExtractor = playlistExtractor;
    }

    @Override
    public boolean canFetch(String uri) {
        return new YouTubeUserCanonicaliser().canonicalise(uri) != null;
    }

    @Override
    public Playlist fetch(String uri) {
        try {
            PlaylistLinkFeed playlist = gdataClient.get(YouTubeUserCanonicaliser.apiUrlFrom(uri));
            
            return playlistExtractor.extract(new YouTubeUserSource(playlist, new YouTubeUserCanonicaliser().canonicalise(uri)));
        } catch (Exception e) {
            throw new FetchException("Failed to fetch: " + uri, e);
        }
    }
}
