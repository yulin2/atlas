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
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.TopicRef.Relationship;
import org.atlasapi.remotesite.knowledgemotion.topics.TopicGuesser;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class KnowledgeMotionDataRowContentExtractorTest {

    private final TopicGuesser topicGuesser = Mockito.mock(TopicGuesser.class);
    private final KnowledgeMotionDataRowContentExtractor extractor = new KnowledgeMotionDataRowContentExtractor(KnowledgeMotionModule.SOURCES, topicGuesser);

    @Test
    public void testExtractItem() {

        ImmutableSet<TopicRef> topicRefs = ImmutableSet.of(new TopicRef(9000l, 1.0f, false, Relationship.ABOUT));
        Mockito.when(topicGuesser.guessTopics(Matchers.anyCollection())).thenReturn(topicRefs);

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
        assertEquals(topicRefs, ImmutableSet.copyOf(item.getTopicRefs()));

        assertFalse(extractor.extract(badRow).isPresent());  // Because it has an incorrect source!
    }
}
