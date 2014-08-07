package org.atlasapi.remotesite.getty;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;

public class GettyAdapterTest {

    private static final String text = "{\"SearchForVideosResult\":{\"Videos\":["
            + "{\"AssetId\":\"id\",\"Caption\":\"description\",\"ClipLength\":\"00:00:10:10\","
            + "\"CollectionId\":\"476\",\"CollectionName\":\"collection\",\"Color\":\"Unknown\","
            + "\"DateCreated\":\"/Date(1406012400000-0700)/\",\"Keywords\":[{\"KeywordId\":\"kid\",\"Text\":\"key\"}],"
            + "\"AspectRatios\":[\"16:9\"], \"Title\":\"title\",\"Urls\":{\"Thumb\":\"thumb\"}}"
            + "]}}";
    
    private final GettyAdapter adapter = new GettyAdapter();
    
    @Test
    public void testAdapter() {
        List<VideoResponse> videos = adapter.parse(text);
        assertEquals(1, videos.size());
        VideoResponse video = Iterables.getOnlyElement(videos);
        assertEquals("id", video.getAssetId());
        assertEquals("/Date(1406012400000-0700)/", video.getDateCreated());
        assertEquals("description", video.getDescription());
        assertEquals("00:00:10:10", video.getDuration());
        assertEquals("key", Iterables.getOnlyElement(video.getKeywords()));
        assertEquals("thumb", video.getThumb());
        assertEquals("title", video.getTitle());
        assertEquals("16:9", Iterables.getOnlyElement(video.getAspectRatios()));
    }
    
}
