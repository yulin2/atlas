package org.atlasapi.remotesite.knowledgemotion.topics.cache;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class KeyphraseTopicCacheRowTranslatorTest {

    @Test
    public void testKeyphraseTopicCacheRowTranslation() {
        KeyphraseTopicCacheRowTranslator translator = new KeyphraseTopicCacheRowTranslator();

        ImmutableList<KeyphraseTopicCacheRow> testRows = ImmutableList.of(
                KeyphraseTopicCacheRow.newCacheRow("test1"),
                KeyphraseTopicCacheRow.loadedCacheRow("test2", Optional.<Optional<String>>absent(), Optional.<Optional<Long>>absent()),
                KeyphraseTopicCacheRow.loadedCacheRow("test3", Optional.of(Optional.<String>absent()), Optional.of(Optional.<Long>absent())),
                KeyphraseTopicCacheRow.loadedCacheRow("test4", Optional.of(Optional.of("something")), Optional.of(Optional.of(5l)))
        );

        for (KeyphraseTopicCacheRow row : testRows) {
            assertEquals(row, translator.fromDBObject(translator.toDBObject(null, row), null));
        }
    }

}
