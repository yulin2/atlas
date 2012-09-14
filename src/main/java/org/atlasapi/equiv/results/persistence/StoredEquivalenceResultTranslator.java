package org.atlasapi.equiv.results.persistence;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.transform;
import static org.atlasapi.media.entity.Identified.TO_URI;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.content.Content;
import org.joda.time.DateTime;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.metabroadcast.common.time.DateTimeZones;

public class StoredEquivalenceResultTranslator {

    public <T extends Content> StoredEquivalenceResult toStoredEquivalenceResult(EquivalenceResult<T> result) {
        ImmutableList.Builder<CombinedEquivalenceScore> totals = ImmutableList.builder();
        Table<String, String, Double> results = HashBasedTable.create();
        
        final Ordering<Entry<T, Score>> equivalenceResultOrdering = Ordering.from(new Comparator<Entry<T, Score>>() {
            @Override
            public int compare(Entry<T, Score> o1, Entry<T, Score> o2) {
                return o1.getKey().getPublisher().compareTo(o2.getKey().getPublisher());
            }
        }).compound(new Comparator<Entry<T, Score>>() {
            @Override
            public int compare(Entry<T, Score> o1, Entry<T, Score> o2) {
                return Score.SCORE_ORDERING.reverse().compare(o1.getValue(), o2.getValue());
            }
        }).compound(new Comparator<Entry<T, Score>>() {
            @Override
            public int compare(Entry<T, Score> o1, Entry<T, Score> o2) {
                return o1.getKey().getCanonicalUri().compareTo(o2.getKey().getCanonicalUri());
            }
        });
        
        Set<String> strongEquivalences = copyOf(transform(transform(result.strongEquivalences().values(), ScoredCandidate.<T>toCandidate()), TO_URI));
        
        for (Entry<T, Score> combinedEquiv : equivalenceResultOrdering.sortedCopy(result.combinedEquivalences().candidates().entrySet())) {
            
            T content = combinedEquiv.getKey();
            
            Double combinedScore = combinedEquiv.getValue().isRealScore() ? combinedEquiv.getValue().asDouble() : Double.NaN;
            totals.add(new CombinedEquivalenceScore(content.getCanonicalUri(), content.getTitle(), combinedScore, strongEquivalences.contains(content.getCanonicalUri()), content.getPublisher().title()));
            for (ScoredCandidates<T> source : result.rawScores()) {
                
                Score sourceScore = source.candidates().get(content);
                Double score = sourceScore != null && sourceScore.isRealScore() ? sourceScore.asDouble() : Double.NaN;
                results.put(content.getCanonicalUri(), source.source(), score);
            }
            
        }
        
        return new StoredEquivalenceResult(result.subject().getCanonicalUri(), result.subject().getTitle(), results, totals.build(), new DateTime(DateTimeZones.UTC), result.description().parts());  
    }
}
