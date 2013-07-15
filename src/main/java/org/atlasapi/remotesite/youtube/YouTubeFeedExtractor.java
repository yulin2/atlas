package org.atlasapi.remotesite.youtube;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.youtube.entity.YouTubeSource;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoEntry;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoFeed;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class YouTubeFeedExtractor implements
        ContentExtractor<YouTubeFeedSource, ContentGroup> {

    private final ContentExtractor<YouTubeSource, Item> itemExtractor;

    public YouTubeFeedExtractor() {
        this(new YouTubeGraphExtractor());
    }

    public YouTubeFeedExtractor(ContentExtractor<YouTubeSource, Item> itemExtractor) {
        this.itemExtractor = itemExtractor;
    }

    public YouTubeFeedExtractor(YouTubeGraphExtractor itemExtractor) {
        this.itemExtractor = itemExtractor;
    }

    @Override
    public ContentGroup extract(YouTubeFeedSource source) {
        YouTubeVideoFeed feed = source.getVideoFeed();
        if (feed == null) {
            return null;
        }

        ContentGroup playlist = new ContentGroup(source.getUri(), Publisher.YOUTUBE);

        playlist.setCurie(YouTubeFeedCanonicaliser.curieFor(source.getUri()));
        playlist.setMediaType(MediaType.VIDEO);

        if (feed.getVideos() != null) {
            Iterable<Item> items = Iterables.transform(feed.getVideos(),
                    new Function<YouTubeVideoEntry, Item>() {

                        @Override
                        public Item apply(YouTubeVideoEntry video) {
                            return itemExtractor.extract(new YouTubeSource(
                                    video, new YoutubeUriCanonicaliser()
                                            .canonicalUriFor(video.getId())));
                        }
                    });
            playlist.setContents(Iterables.transform(items, Item.TO_CHILD_REF));
        }
        return playlist;
    }
}
