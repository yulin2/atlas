package org.atlasapi.equiv.tasks;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Version;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.stats.Count;

public abstract class EquivResult {

    protected List<EquivResult> subResults;

    public static EquivResult of(Brand brand, Iterable<Count<Brand>> countedEquivBrands) {
        return Iterables.isEmpty(countedEquivBrands) ? new EmptyEquivResult(brand, brand.getContents().size()) : new MatchedEquivResult(brand, brand.getContents().size(), asMap(countedEquivBrands));
    }

    public static EquivResult of(Episode episode, Iterable<Count<Episode>> countedEquivEpisodes) {
        int broadcasts = countBroadcasts(episode);
        return Iterables.isEmpty(countedEquivEpisodes) ? new EmptyEquivResult(episode, broadcasts) : new MatchedEquivResult(episode, broadcasts, asMap(countedEquivEpisodes));
    }
    
    private static int countBroadcasts(Episode episode) {
        int broadcasts = 0;
        for (Version version : episode.getVersions()) {
            broadcasts += version.getBroadcasts().size();
        }
        return broadcasts;
    }

    private static <T extends Described> Map<T, Long> asMap(Iterable<Count<T>> countedEquivDescs) {
        return Maps.transformValues(Maps.uniqueIndex(countedEquivDescs, Count.<T>unpackTarget()), new Function<Count<T>, Long>() {
            @Override
            public Long apply(Count<T> input) {
                return input.getCount();
            }});
    }
    
    private static class MatchedEquivResult extends EquivResult {

        private final Described brand;
        private final int maxMatch;
        private final Map<? extends Described, Long> equivs;

        public MatchedEquivResult(Described desc, int maxMatch, Map<? extends Described, Long> equivs) {
            this.brand = desc;
            this.maxMatch = maxMatch;
            this.equivs = equivs;
        }
        
        @Override
        public boolean equals(Object that) {
            if(this == that) {
                return true;
            }
            if(that instanceof MatchedEquivResult) {
                MatchedEquivResult other = (MatchedEquivResult) that;
                return Objects.equal(brand, other.brand) && Objects.equal(equivs, other.equivs);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(brand, equivs);
        }
        
        @Override
        public String toString() {
            return String.format("%s/%s:%s, equivalent to %s", brand.getTitle(), brand.getCanonicalUri(), maxMatch, toString(equivs));
        }
        
        private String toString(Map<? extends Described, Long> brands) {
            return Joiner.on(", ").join(Iterables.transform(brands.entrySet(), new Function<Entry<? extends Described, Long>, String>() {
                @Override
                public String apply(Entry<? extends Described, Long> input) {
                    return String.format("%s/%s:%s\n%s", input.getKey().getTitle(), input.getKey().getCanonicalUri(), input.getValue(), subResultsString());
                }
            }));
        }
    }
    
    private static class EmptyEquivResult extends EquivResult {

        private final Described desc;
        private final int maxMatch;

        public EmptyEquivResult(Described desc, int maxMatch) {
            this.desc = desc;
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
    
    protected EquivResult withSubResults(Iterable<EquivResult> subResults) {
        this.subResults = ImmutableList.copyOf(subResults);
        return this;
    }
    
    protected String subResultsString() {
        return "\t" + subResults == null ? "" : Joiner.on('\t').join(subResults);
    }
}
