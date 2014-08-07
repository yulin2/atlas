package org.atlasapi.remotesite.getty;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class GettyContentExtractorTest {

    private final GettyContentExtractor extractor = new GettyContentExtractor();
    
    @Test
    public void testExtractItem() {
        
        VideoResponse video = new VideoResponse();
        video.setAssetId("id");
        video.setDateCreated("/Date(1406012400000+0700)/");
        video.setDescription("description");
        video.setDuration("00:01:01:10");
        video.setKeywords(ImmutableList.of("key"));
        video.setThumb("thumb");
        video.setTitle("title");
        video.setAspectRatios(ImmutableList.of("16:9"));
        
        Content content = extractor.extract(video);
        Item item = (Item) content;
        assertThat(item.getCanonicalUri(), endsWith("id"));
        assertEquals(item.getPublisher(), Publisher.GETTY);
        assertEquals("description", item.getDescription());
        assertEquals("title", item.getTitle());
        assertEquals("title", item.getTitle());
        assertEquals(MediaType.VIDEO, content.getMediaType());
        assertEquals(new KeyPhrase("key", Publisher.GETTY), Iterables.getOnlyElement(content.getKeyPhrases()));
        assertEquals("thumb", content.getThumbnail());
        assertEquals("thumb", content.getImage());
        assertEquals(Integer.valueOf(61), Iterables.getOnlyElement(item.getVersions()).getDuration());
        assertEquals("16:9", Iterables.getOnlyElement(
                Iterables.getOnlyElement(item.getVersions()).getManifestedAs())
                .getVideoAspectRatio());
    }
    
}
