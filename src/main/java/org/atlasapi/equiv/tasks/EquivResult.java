package org.atlasapi.equiv.tasks;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Version;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.stats.Count;

public abstract class EquivResult {

    protected List<EquivResult> subResults;
    protected final Described desc;

    public EquivResult(Described desc) {
        this.desc = desc;
    }

    public static EquivResult of(Brand brand, EquivalentSuggestions<Brand> countedEquivBrands, double certainty) {
        return countedEquivBrands.getBinnedCountedSuggestions().isEmpty() ? new EmptyEquivResult(brand, brand.getContents().size()) : new MatchedEquivResult(brand, brand.getContents().size(), countedEquivBrands, certainty);
    }

    public static EquivResult of(Episode episode, EquivalentSuggestions<Episode> countedEquivEps, double certainty) {
        int broadcasts = countBroadcasts(episode);
        return countedEquivEps.getBinnedCountedSuggestions().isEmpty() ? new EmptyEquivResult(episode, broadcasts) : new MatchedEquivResult(episode, broadcasts, countedEquivEps, certainty);
    }
    
    private static int countBroadcasts(Episode episode) {
        int broadcasts = 0;
        for (Version version : episode.getVersions()) {
            broadcasts += version.getBroadcasts().size();
        }
        return broadcasts;
    }
    
    private static class MatchedEquivResult extends EquivResult {

        private final int maxMatch;
        private final EquivalentSuggestions<? extends Described> equivs;
        private final double certainty;

        public MatchedEquivResult(Described desc, int maxMatch, EquivalentSuggestions<? extends Described> equivs, double certainty) {
            super(desc);
            this.maxMatch = maxMatch;
            this.equivs = equivs;
            this.certainty = certainty;
        }
        
        @Override
        public boolean equals(Object that) {
            if(this == that) {
                return true;
            }
            if(that instanceof MatchedEquivResult) {
                MatchedEquivResult other = (MatchedEquivResult) that;
                return Objects.equal(desc, other.desc) && Objects.equal(equivs, other.equivs);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(desc, equivs);
        }
        
        @Override
        public String toString() {
            return String.format("%s/%s:%s, equivalent to %s\n%s", desc.getTitle(), desc.getCanonicalUri(), maxMatch, equivString(equivs), subResultsString());
        }
        
        public <T extends Described> String equivString(EquivalentSuggestions<T> equivs) {
            final Set<Count<T>> certainSuggestions = equivs.allMatchedSuggestions(certainty);
            return Joiner.on(", ").join(Iterables.transform(equivs.allSuggestions(), new Function<Count<T>, String>() {
                @Override
                public String apply(Count<T> input) {
                    T target = input.getTarget();
                    return String.format("%s(%s):%s%s", target.getTitle(), target.getCanonicalUri(), input.getCount(), certainSuggestions.contains(input) ? " MATCHED" : ""); 
                }
            }));
        }
    }
    
    private static class EmptyEquivResult extends EquivResult {

        private final int maxMatch;

        public EmptyEquivResult(Described desc, int maxMatch) {
            super(desc);
            this.maxMatch = maxMatch;
        }
        
        @Override
        public boolean equals(Object that) {
            if(this == that) {
                return true;
            }
            if(that instanceof EmptyEquivResult) {
                EmptyEquivResult other = (EmptyEquivResult) that;
                return Objects.equal(desc, other.desc);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(desc);
        }
        
        @Override
        public String toString() {
            return String.format("%s/%s:%s, equivalent to nothing\n%s", desc.getTitle(), desc.getCanonicalUri(), maxMatch, subResultsString());
        }
    }
    
    public Described described() {
        return desc;
    }
    
    public EquivResult withSubResults(Iterable<EquivResult> subResults) {
        this.subResults = ImmutableList.copyOf(subResults);
        return this;
    }
    
    protected String subResultsString() {
        return subResults == null ? "" : "\t" + Joiner.on('\t').join(subResults);
    }
}
