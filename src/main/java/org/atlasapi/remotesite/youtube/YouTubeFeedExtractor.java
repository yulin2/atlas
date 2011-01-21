package org.atlasapi.remotesite.youtube;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;

public class YouTubeFeedExtractor implements ContentExtractor<YouTubeFeedSource, Playlist> {
    
    private final ContentExtractor<YouTubeSource, Item> itemExtractor;
    
    public YouTubeFeedExtractor() {
        this(new YouTubeGraphExtractor());
    }

    public YouTubeFeedExtractor(ContentExtractor<YouTubeSource, Item> itemExtractor) {
        this.itemExtractor = itemExtractor;
    }

    @Override
    public Playlist extract(YouTubeFeedSource source) {
        VideoFeed feed = source.getVideoFeed();
        
        Playlist playlist = new Playlist(source.getUri(), YouTubeFeedCanonicaliser.curieFor(source.getUri()), Publisher.YOUTUBE);
        playlist.setMediaType(MediaType.VIDEO);
        
        for (VideoEntry video: feed.getEntries()) {
            Item item = itemExtractor.extract(new YouTubeSource(video, new YoutubeUriCanonicaliser().canonicalise(video.getId())));
            playlist.addItem(item);
        }
        
        return playlist;
    }
}
