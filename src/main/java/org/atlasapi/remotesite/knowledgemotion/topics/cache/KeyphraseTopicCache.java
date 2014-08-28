package org.atlasapi.remotesite.knowledgemotion.topics.cache;

public class KeyphraseTopicCache {

    public KeyphraseTopicCacheRow get(String keyPhrase) {
        // TODO load
        return KeyphraseTopicCacheRow.newCacheRow(keyPhrase);
    }

    public void update(KeyphraseTopicCacheRow cacheRow) {
        // TODO save
    }

}
