package org.atlasapi.remotesite.youtube;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;

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
        YouTubeFeedClient.VideoFeed feed = source.getVideoFeed();
        
        Playlist playlist = new Playlist(source.getUri(), YouTubeFeedCanonicaliser.curieFor(source.getUri()), Publisher.YOUTUBE);
        playlist.setMediaType(MediaType.VIDEO);
        
        for (YouTubeFeedClient.VideoEntry video: feed.videos) {
            Item item = itemExtractor.extract(new YouTubeSource(video, new YoutubeUriCanonicaliser().canonicalUriFor(video.id)));
            playlist.addItem(item);
        }
        
        return playlist;
    }
}
