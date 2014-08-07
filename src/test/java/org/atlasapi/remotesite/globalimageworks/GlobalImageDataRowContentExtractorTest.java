package org.atlasapi.remotesite.globalimageworks;

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

public class GlobalImageDataRowContentExtractorTest {

    private final GlobalImageDataRowContentExtractor extractor = new GlobalImageDataRowContentExtractor();
    
    @Test
    public void testExtractItem() {
        
        GlobalImageDataRow row = GlobalImageDataRow.builder()
                .withDate("2014-01-01")
                .withDescription("description")
                .withDuration("0:01:01;10")
                .withId("id")
                .withKeywords(ImmutableList.of("key"))
                .withSource("GlobalImageworks")
                .withTitle("title")
                .withKeywords(ImmutableList.of("key"))
                .build();
        
        Content content = extractor.extract(row);
        Item item = (Item) content;
        assertThat(item.getCanonicalUri(), endsWith("id"));
        assertEquals(item.getPublisher(), Publisher.GLOBALIMAGEWORKS);
        assertEquals("description", item.getDescription());
        assertEquals("title", item.getTitle());
        assertEquals(MediaType.VIDEO, item.getMediaType());
        assertEquals(Integer.valueOf(61), Iterables.getOnlyElement(item.getVersions()).getDuration());
        assertEquals(new KeyPhrase("key", Publisher.GLOBALIMAGEWORKS), Iterables.getOnlyElement(item.getKeyPhrases()));
    }
    
}
