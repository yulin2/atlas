package org.atlasapi.remotesite.youtube.user;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.youtube.YouTubeGraphExtractor;
import org.atlasapi.remotesite.youtube.YouTubeSource;
import org.atlasapi.remotesite.youtube.YoutubeUriCanonicaliser;

import com.google.common.collect.Lists;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.VideoEntry;

public class YouTubeUserExtractor implements ContentExtractor<YouTubeUserSource, ContentGroup> {

    private final ContentExtractor<YouTubeSource, Item> itemExtractor;
    private final YouTubePlaylistClient playlistClient;
    private static final Log LOG = LogFactory.getLog(YouTubeUserExtractor.class);

    public YouTubeUserExtractor() {
        this(new YouTubeGraphExtractor(), new YouTubePlaylistClient());
    }

    public YouTubeUserExtractor(ContentExtractor<YouTubeSource, Item> itemExtractor, YouTubePlaylistClient playlistClient) {
        this.itemExtractor = itemExtractor;
        this.playlistClient = playlistClient;
    }

    @Override
    public ContentGroup extract(YouTubeUserSource source) {
        PlaylistLinkFeed feed = source.getPlaylistFeed();

        if (LOG.isInfoEnabled()) {
            LOG.info("Retrieving playlists for user: "+source.getUri());
        }
        ContentGroup userPlaylist = new ContentGroup(source.getUri(), YouTubeUserCanonicaliser.curieFor(source.getUri()), Publisher.YOUTUBE);

        for (PlaylistLinkEntry entry : feed.getEntries()) {
            String playlistUrl = entry.getFeedUrl();
            // Stopping sub-playlist creation, as they are impossible to reference
            // Playlist playlist = new Playlist(playlistUrl, YouTubePlaylistCanonicaliser.curieFor(playlistUrl), Publisher.YOUTUBE);

            try {
                PlaylistFeed playlistFeed = playlistClient.get(playlistUrl);
                List<Item> items = Lists.newArrayList();
                for (VideoEntry video : playlistFeed.getEntries()) {
                    if (video != null && video.getHtmlLink() != null && video.getHtmlLink().getHref() != null) {
                        Item item = itemExtractor.extract(new YouTubeSource(video, new YoutubeUriCanonicaliser().canonicalise(video.getHtmlLink().getHref())));
                        items.add(item);
                        // playlist.addItem(item);
                    }
                }
                userPlaylist.setContents(items);
            } catch (Exception e) {
                throw new FetchException("Unable to retrieve playlist: "+playlistUrl, e);
            }
            
            // userPlaylist.addPlaylist(playlist);
        }

        return userPlaylist;
    }
}
