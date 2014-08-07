package org.atlasapi.remotesite.getty;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.io.Resources;

public class GettyAdapterTest {

    private final GettyAdapter adapter = new GettyAdapter();
    
    @Test
    public void testConversionFromJson() throws IOException {
        String text = FileUtils.readFileToString(new File(Resources.getResource(getClass(), "getty.json").getFile()));
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
