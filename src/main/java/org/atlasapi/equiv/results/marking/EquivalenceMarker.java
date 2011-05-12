package org.atlasapi.equiv.results.marking;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

public interface EquivalenceMarker<T extends Content> {

    List<ScoredEquivalent<T>> mark(List<ScoredEquivalent<T>> equivalents);
    
}
