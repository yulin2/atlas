package org.atlasapi.remotesite.youtube;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.ContentType;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;

public class YouTubeFeedExtractor implements ContentExtractor<YouTubeFeedSource, ContentGroup> {
    
    private final ContentExtractor<YouTubeSource, Item> itemExtractor;
    
    public YouTubeFeedExtractor() {
        this(new YouTubeGraphExtractor());
    }

    public YouTubeFeedExtractor(ContentExtractor<YouTubeSource, Item> itemExtractor) {
        this.itemExtractor = itemExtractor;
    }

    @Override
    public ContentGroup extract(YouTubeFeedSource source) {
        VideoFeed feed = source.getVideoFeed();
        
        ContentGroup playlist = new ContentGroup(source.getUri(), YouTubeFeedCanonicaliser.curieFor(source.getUri()), Publisher.YOUTUBE);
        playlist.setContentType(ContentType.VIDEO);
        
        Iterable<Item> items = Iterables.transform(feed.getEntries(), new Function<VideoEntry, Item>() {

			@Override
			public Item apply(VideoEntry video) {
				return itemExtractor.extract(new YouTubeSource(video, new YoutubeUriCanonicaliser().canonicalise(video.getId())));
			}
        });
        
        playlist.setContents(items);
        return playlist;
    }
}
