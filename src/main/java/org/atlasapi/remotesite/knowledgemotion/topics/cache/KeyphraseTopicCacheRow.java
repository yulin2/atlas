package org.atlasapi.remotesite.knowledgemotion.topics.cache;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Optional;

public class KeyphraseTopicCacheRow {

    private final String keyPhrase;
    private final Optional<Optional<String>> maybeArticleNameLookupResult;
    private final Optional<Optional<Long>> maybeTopicIdLookupResult;

    private KeyphraseTopicCacheRow(String keyPhrase, Optional<Optional<String>> maybeArticleNameLookupResult, Optional<Optional<Long>> maybeTopicIdLookupResult) {
        this.keyPhrase = checkNotNull(emptyToNull(keyPhrase));
        this.maybeArticleNameLookupResult = checkNotNull(maybeArticleNameLookupResult);
        this.maybeTopicIdLookupResult = checkNotNull(maybeTopicIdLookupResult);
    }

    static KeyphraseTopicCacheRow newCacheRow(String keyPhrase) {
        return new KeyphraseTopicCacheRow(keyPhrase, Optional.<Optional<String>>absent(), Optional.<Optional<Long>>absent());
    }

    static KeyphraseTopicCacheRow loadedCacheRow(String keyPhrase, Optional<Optional<String>> maybeArticleNameLookupResult, Optional<Optional<Long>> maybeTopicIdLookupResult) {
        return new KeyphraseTopicCacheRow(keyPhrase, maybeArticleNameLookupResult, maybeTopicIdLookupResult);
    }

    public String getKeyPhrase() {
        return keyPhrase;
    }

    public Optional<Optional<String>> getPossibleArticleNameResult() {
        return maybeArticleNameLookupResult;
    }

    public Optional<Optional<Long>> getPossibleTopicIdResult() {
        return maybeTopicIdLookupResult;
    }

    public KeyphraseTopicCacheRow withArticleNameResult(Optional<String> articleNameLookupResult) {
        return new KeyphraseTopicCacheRow(keyPhrase, Optional.of(articleNameLookupResult), maybeTopicIdLookupResult);
    }

    public KeyphraseTopicCacheRow withTopicIdResult(Optional<Long> topicIdLookupResult) {
        return new KeyphraseTopicCacheRow(keyPhrase, maybeArticleNameLookupResult, Optional.of(topicIdLookupResult));
    }
}
