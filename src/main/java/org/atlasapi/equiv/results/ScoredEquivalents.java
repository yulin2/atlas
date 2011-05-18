package org.atlasapi.equiv.results;

import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

public interface ScoredEquivalents<T extends Content> {

    String source();

    Map<Publisher, Map<T, Double>> equivalents();

}