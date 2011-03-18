package org.atlasapi.equiv.tasks;

import java.util.Map;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;

import com.google.common.base.Function;
import com.google.common.base.Objects;

public class EquivResult<T> {
    
    public static EquivResult<Item> of(Item episode, SuggestedEquivalents<Item> countedEquivEps, double certainty) {
        int broadcasts = 0;
        for (Version version : episode.getVersions()) {
            broadcasts += version.getBroadcasts().size();
        }
        return new EquivResult<Item>(episode, broadcasts, countedEquivEps, certainty);
    }

    private final T desc;
    private final int fullMatch;
    private final SuggestedEquivalents<T> suggestedEquivs;
    private final double certainty;

    public EquivResult(T desc, int fullMatch, SuggestedEquivalents<T> suggestedEquivs, double certainty) {
        this.desc = desc;
        this.fullMatch = fullMatch;
        this.suggestedEquivs = suggestedEquivs;
        this.certainty = certainty;
    }

    public T described() {
        return desc;
    }

    public double certainty() {
        return certainty;
    }

    public int fullMatch() {
        return fullMatch;
    }

    public Map<Publisher, T> strongSuggestions() {
        return suggestedEquivs.strongSuggestions(certainty);
    }

    public SuggestedEquivalents<T> suggestedEquivalents() {
        return suggestedEquivs;
    }
    
    public <V extends Comparable<? super V>> EquivResult<V> transformResult(Function<? super T, V> transformer) {
        return new EquivResult<V>(transformer.apply(desc), fullMatch, suggestedEquivs.transform(transformer), certainty);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof EquivResult) {
            EquivResult<?> other = (EquivResult<?>) that;
            return Objects.equal(desc, other.desc) && Objects.equal(suggestedEquivs, other.suggestedEquivs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(desc, suggestedEquivs);
    }

    @Override
    public String toString() {
        return String.format("Equiv result for %s");
    }
}
