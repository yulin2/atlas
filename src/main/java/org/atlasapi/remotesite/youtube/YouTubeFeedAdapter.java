package org.atlasapi.remotesite.youtube;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.youtube.entity.YouTubeVideoFeed;

import com.metabroadcast.common.http.HttpStatusCodeException;

public class YouTubeFeedAdapter implements SiteSpecificAdapter<ContentGroup> {

    private final RemoteSiteClient<YouTubeVideoFeed> gdataClient;
    private final ContentExtractor<YouTubeFeedSource, ContentGroup> feedExtractor;

    public YouTubeFeedAdapter() {
        this(new YouTubeFeedClient(), new YouTubeFeedExtractor());
    }

    public YouTubeFeedAdapter(RemoteSiteClient<YouTubeVideoFeed> gdataClient,
            ContentExtractor<YouTubeFeedSource, ContentGroup> feedExtractor) {
        this.gdataClient = gdataClient;
        this.feedExtractor = feedExtractor;
    }

    @Override
    public boolean canFetch(String uri) {
        return new YouTubeFeedCanonicaliser().canonicalise(uri) != null;
    }

    @Override
    public ContentGroup fetch(String uri) {
        try {
            YouTubeVideoFeed feed = gdataClient.get(uri);

            return feedExtractor.extract(new YouTubeFeedSource(feed, uri));
        } catch (HttpStatusCodeException e) {
            return null;
        } catch (Exception e) {
            throw new FetchException("Failed to fetch: " + uri, e);
        }
    }
}
