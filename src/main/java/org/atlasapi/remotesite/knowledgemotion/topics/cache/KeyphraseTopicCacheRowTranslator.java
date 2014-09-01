package org.atlasapi.remotesite.knowledgemotion.topics.cache;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.translator.ModelTranslator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class KeyphraseTopicCacheRowTranslator implements ModelTranslator<KeyphraseTopicCacheRow> {

    private static final String KEY_ARTICLE_TITLE = "articleTitle";
    private static final String KEY_TOPIC_ID = "topicId";

    /**
     * Indicates caching an absent result, as opposed to having no result cached (null).
     */
    private static final String VALUE_EMPTY_RESULT = "::none::";

    @Override
    public KeyphraseTopicCacheRow fromDBObject(DBObject dbObject, KeyphraseTopicCacheRow model) {
        String keyPhrase = (String) dbObject.get(ID);

        String maybeArticleTitle = (String) dbObject.get(KEY_ARTICLE_TITLE);
        Optional<Optional<String>> modelMaybeArticleTitle;
        if (maybeArticleTitle == null) {
            modelMaybeArticleTitle = Optional.absent();
        } else if (VALUE_EMPTY_RESULT.equals(maybeArticleTitle)) {
            modelMaybeArticleTitle = Optional.<Optional<String>>of(Optional.<String>absent());
        } else {
            modelMaybeArticleTitle = Optional.of(Optional.of(maybeArticleTitle));
        }

        Object maybeTopicId = dbObject.get(KEY_TOPIC_ID);
        Optional<Optional<Long>> modelMaybeTopicId;
        if (maybeTopicId == null) {
            modelMaybeTopicId = Optional.absent();
        } else if (maybeTopicId instanceof String && VALUE_EMPTY_RESULT.equals(maybeTopicId)) {
            modelMaybeTopicId = Optional.<Optional<Long>>of(Optional.<Long>absent());
        } else {
            modelMaybeTopicId = Optional.of(Optional.of((Long) maybeTopicId));
        }

        return KeyphraseTopicCacheRow.loadedCacheRow(keyPhrase, modelMaybeArticleTitle, modelMaybeTopicId);
    }

    @Override
    public DBObject toDBObject(DBObject dbObject, KeyphraseTopicCacheRow model) {
        dbObject = new BasicDBObject();
        dbObject.put(ID, model.getKeyPhrase());

        Optional<Optional<String>> possibleArticleNameResult = model.getPossibleArticleNameResult();
        if (possibleArticleNameResult.isPresent()) {
            dbObject.put(KEY_ARTICLE_TITLE, possibleArticleNameResult.get().or(VALUE_EMPTY_RESULT));
        }

        Optional<Optional<Long>> possibleTopicIdResult = model.getPossibleTopicIdResult();
        if (possibleTopicIdResult.isPresent()) {
            Optional<Long> topicIdResult = possibleTopicIdResult.get();
            if (topicIdResult.isPresent()) {
                dbObject.put(KEY_TOPIC_ID, topicIdResult.get());
            } else {
                dbObject.put(KEY_TOPIC_ID, VALUE_EMPTY_RESULT);
            }
        }

        return dbObject;
    }

}
