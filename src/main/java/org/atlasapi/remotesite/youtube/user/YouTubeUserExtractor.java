package org.atlasapi.remotesite.youtube.user;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.youtube.YouTubeGraphExtractor;
import org.atlasapi.remotesite.youtube.YouTubeSource;
import org.atlasapi.remotesite.youtube.YoutubeUriCanonicaliser;

import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.VideoEntry;

public class YouTubeUserExtractor implements ContentExtractor<YouTubeUserSource, Playlist> {

    private final ContentExtractor<YouTubeSource, Item> itemExtractor;
    private final YouTubePlaylistClient playlistClient;

    public YouTubeUserExtractor() {
        this(new YouTubeGraphExtractor(), new YouTubePlaylistClient());
    }

    public YouTubeUserExtractor(ContentExtractor<YouTubeSource, Item> itemExtractor, YouTubePlaylistClient playlistClient) {
        this.itemExtractor = itemExtractor;
        this.playlistClient = playlistClient;
    }

    @Override
    public Playlist extract(YouTubeUserSource source) {
        PlaylistLinkFeed feed = source.getPlaylistFeed();

        Playlist userPlaylist = new Playlist(source.getUri(), YouTubeUserCanonicaliser.curieFor(source.getUri()), Publisher.YOUTUBE);

        for (PlaylistLinkEntry entry : feed.getEntries()) {
            String playlistUrl = entry.getFeedUrl();
            Playlist playlist = new Playlist(playlistUrl, YouTubePlaylistCanonicaliser.curieFor(playlistUrl), Publisher.YOUTUBE);

            try {
                PlaylistFeed playlistFeed = playlistClient.get(playlistUrl);
                for (VideoEntry video : playlistFeed.getEntries()) {
                    Item item = itemExtractor.extract(new YouTubeSource(video, new YoutubeUriCanonicaliser().canonicalise(video.getId())));
                    playlist.addItem(item);
                }
            } catch (Exception e) {
                throw new FetchException("Unable to retrieve playlist: "+playlistUrl, e);
            }
            
            userPlaylist.addPlaylist(playlist);
        }

        return userPlaylist;
    }
}
