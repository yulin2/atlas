package org.atlasapi.equiv.results;

import java.util.Map;

import org.atlasapi.media.entity.Content;

public interface ScoredEquivalents<T extends Content> {

    String source();

    Map<T, Score> equivalents();

}