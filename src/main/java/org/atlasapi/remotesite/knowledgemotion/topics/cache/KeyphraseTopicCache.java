package org.atlasapi.remotesite.knowledgemotion.topics.cache;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class KeyphraseTopicCache {

    private static final String COLLECTION_NAME = "spotlightTopicsCache";

    private final DBCollection collection;
    private final KeyphraseTopicCacheRowTranslator translator = new KeyphraseTopicCacheRowTranslator();

    public KeyphraseTopicCache(DatabasedMongo mongo) {
        this.collection = mongo.collection(COLLECTION_NAME);
    }

    public KeyphraseTopicCacheRow get(String keyPhrase) {
        DBObject maybeFound = collection.findOne(keyPhrase);
        if (maybeFound == null) {
            return KeyphraseTopicCacheRow.newCacheRow(keyPhrase);
        }
        return translator.fromDBObject(maybeFound, null);
    }

    public void update(KeyphraseTopicCacheRow cacheRow) {
        collection.save(translator.toDBObject(null, cacheRow));
    }

}
