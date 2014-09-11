package org.atlasapi.remotesite.knowledgemotion.topics;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.TopicRef.Relationship;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.knowledgemotion.topics.cache.KeyphraseTopicCache;
import org.atlasapi.remotesite.knowledgemotion.topics.cache.KeyphraseTopicCacheRow;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class TopicGuesser {

    private final KeywordsExtractor keywordsExtractor;
    private final KeyphraseTopicCache cache;
    private final TopicStore topicStore;

    public TopicGuesser(KeywordsExtractor keywordsExtractor, KeyphraseTopicCache cache, TopicStore topicStore) {
        this.keywordsExtractor = checkNotNull(keywordsExtractor);
        this.cache = checkNotNull(cache);
        this.topicStore = checkNotNull(topicStore);
    }

    public ImmutableSet<TopicRef> guessTopics(Iterable<String> keyPhrases) {
        ImmutableSet.Builder<Long> topicIds = ImmutableSet.builder();
        for (String keyPhrase : keyPhrases) {
            KeyphraseTopicCacheRow cacheRow = cache.get(keyPhrase);

            Optional<Optional<Long>> possibleCachedTopicIdResult = cacheRow.getPossibleTopicIdResult();
            Optional<Long> topicIdResult;
            if (possibleCachedTopicIdResult.isPresent()) {
                topicIdResult = possibleCachedTopicIdResult.get();
            }
            else {
                Optional<Optional<String>> possibleCachedArticleNameResult = cacheRow.getPossibleArticleNameResult();
                Optional<String> articleNameResult;
                if (possibleCachedArticleNameResult.isPresent()) {
                    articleNameResult = possibleCachedArticleNameResult.get();
                } else {
                    Optional<WikipediaKeyword> maybeKeyword = keywordsExtractor.tryGuessSingleKeywordFromText(keyPhrase);
                    articleNameResult = maybeKeyword.isPresent()
                            ? Optional.of(maybeKeyword.get().getArticleTitle())
                            : Optional.<String>absent();
                    cacheRow = cacheRow.withArticleNameResult(articleNameResult);
                }

                if (articleNameResult.isPresent()) {
                    topicIdResult = tryGetTopicIdFromArticleName(articleNameResult.get());
                } else {
                    topicIdResult = Optional.absent();
                }

                cacheRow = cacheRow.withTopicIdResult(topicIdResult);
                cache.update(cacheRow);
            }

            if (topicIdResult.isPresent()) {
                topicIds.add(topicIdResult.get());
            }
        }

        return ImmutableSet.copyOf(Iterables.transform(topicIds.build(), new Function<Long, TopicRef>(){
            @Override
            public TopicRef apply(Long topicId) {
                return new TopicRef(topicId, 1.0f, false, Relationship.ABOUT);
            }
        }));
    }

    private Optional<Long> tryGetTopicIdFromArticleName(String articleName) {
        Maybe<Topic> maybeTopic = topicStore.topicFor(Publisher.DBPEDIA, "dbpedia", "http://dbpedia.org/resource/" + articleName);
        if (maybeTopic.isNothing()) {
            return Optional.absent();
        }
        Topic topic = maybeTopic.requireValue();

        topic.setPublisher(Publisher.DBPEDIA);
        topic.setTitle(articleName);
        topicStore.write(topic);
        return Optional.of(topic.getId());
    }

}
