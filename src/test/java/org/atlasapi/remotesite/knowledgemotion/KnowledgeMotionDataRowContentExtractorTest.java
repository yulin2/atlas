package org.atlasapi.remotesite.knowledgemotion;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class KnowledgeMotionDataRowContentExtractorTest {

    private final KnowledgeMotionDataRowContentExtractor extractor = new KnowledgeMotionDataRowContentExtractor(KnowledgeMotionModule.SOURCES);
    
    @Test
    public void testExtractItem() {

        KnowledgeMotionDataRow row = KnowledgeMotionDataRow.builder()
                .withDate("2014-01-01")
                .withDescription("description")
                .withDuration("0:01:01;10")
                .withId("id")
                .withKeywords(ImmutableList.of("key"))
                .withSource("GlobalImageworks")
                .withTitle("title")
                .withKeywords(ImmutableList.of("key"))
                .build();

        KnowledgeMotionDataRow badRow = KnowledgeMotionDataRow.builder()
                .withDate("2014-01-01")
                .withDescription("description")
                .withDuration("0:01:01;10")
                .withId("id")
                .withKeywords(ImmutableList.of("key"))
                .withSource("GlobalInageworks")
                .withTitle("title")
                .withKeywords(ImmutableList.of("key"))
                .build();

        Optional<? extends Content> content = extractor.extract(row);
        Item item = (Item) content.get();
        assertThat(item.getCanonicalUri(), endsWith("id"));
        assertEquals(item.getPublisher(), Publisher.KM_GLOBALIMAGEWORKS);
        assertEquals("description", item.getDescription());
        assertEquals("title", item.getTitle());
        assertEquals(MediaType.VIDEO, item.getMediaType());
        assertEquals(Integer.valueOf(61), Iterables.getOnlyElement(item.getVersions()).getDuration());
        assertEquals(new KeyPhrase("key", Publisher.KM_GLOBALIMAGEWORKS), Iterables.getOnlyElement(item.getKeyPhrases()));

        assertFalse(extractor.extract(badRow).isPresent());  // Because it has an incorrect source!
    }
}
