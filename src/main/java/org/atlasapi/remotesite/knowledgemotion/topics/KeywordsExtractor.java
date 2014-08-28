package org.atlasapi.remotesite.knowledgemotion.topics;

import java.util.List;

import com.google.common.base.Optional;

public interface KeywordsExtractor {

    List<WikipediaKeyword> extractKeywordsFromText(String text);

    Optional<WikipediaKeyword> tryGuessSingleKeywordFromText(String text);

}
