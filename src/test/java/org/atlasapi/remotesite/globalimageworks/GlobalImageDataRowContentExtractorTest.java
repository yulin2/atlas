package org.atlasapi.remotesite.globalimageworks;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class GlobalImageDataRowContentExtractorTest {

    private final GlobalImageDataRowContentExtractor extractor = new GlobalImageDataRowContentExtractor();
    
    @Test
    public void testExtractItem() {
        
        GlobalImageDataRow row = GlobalImageDataRow.builder()
                .withDate("2014-01-01")
                .withDescription("description")
                .withDuration("0:01:55")
                .withId("id")
                .withKeywords(ImmutableList.of("key"))
                .withSource("GlobalImageworks")
                .withTitle("title")
                .build();
        
        Content content = extractor.extract(row);
        Item item = (Item) content;
        assertThat(item.getCanonicalUri(), endsWith("id"));
        assertEquals(item.getPublisher(), Publisher.GLOBALIMAGEWORKS);
        assertEquals("description", item.getDescription());
        assertEquals("title", item.getTitle());
    }
    
}
