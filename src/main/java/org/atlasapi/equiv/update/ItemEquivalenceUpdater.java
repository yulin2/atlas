package org.atlasapi.equiv.update;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContentEquivalenceScorer;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ItemEquivalenceUpdater implements ContentEquivalenceUpdater<Item> {

    private final Iterable<ContentEquivalenceGenerator<Item>> generators;
    private final Iterable<ContentEquivalenceScorer<Item>> scorers;
    private final EquivalenceResultBuilder<Item> resultBuilder;
    private final AdapterLog log;

    public ItemEquivalenceUpdater(Iterable<ContentEquivalenceGenerator<Item>> generators, Iterable<ContentEquivalenceScorer<Item>> scorers, EquivalenceResultBuilder<Item> resultBuilder, AdapterLog log) {
        this.generators = generators;
        this.scorers = scorers;
        this.resultBuilder = resultBuilder;
        this.log = log;
    }
    
    @Override
    public EquivalenceResult<Item> updateEquivalences(Item content) {
        
        Map<String,ScoredEquivalents<Item>> generatedScores = Maps.newHashMap();
        
        for (ContentEquivalenceGenerator<Item> generator : generators) {
            
            ScoredEquivalents<Item> generated = generator.generate(content);
            generatedScores.put(generated.source(), generated);
            
        }
        
        List<Item> generatedSuggestions = extractGeneratedSuggestions(generatedScores.values());
        
        Map<String, ScoredEquivalents<Item>> scoredScores = Maps.newHashMap();
        
        for (ContentEquivalenceScorer<Item> scorer : scorers) {
            
            ScoredEquivalents<Item> scored = scorer.score(content, generatedSuggestions);
            scoredScores.put(scored.source(), scored);
            
        }
        
        return resultBuilder.resultFor(content, ImmutableList.copyOf(merge(generatedScores, scoredScores).values()));
    }

    private Map<String, ScoredEquivalents<Item>> merge(Map<String, ScoredEquivalents<Item>> left, Map<String, ScoredEquivalents<Item>> right) {
        
        Map<String, ScoredEquivalents<Item>> merged = Maps.newHashMap();
        
        for (String source : Iterables.concat(left.keySet(), right.keySet())) {
            if(!left.containsKey(source)) {
                merged.put(source, right.get(source));
            } else if(!right.containsKey(source)) {
                merged.put(source, left.get(source));
            } else {
                merged.put(source, merge(left.get(source), (right.get(source))));
            }
        }
        
        return null;
    }

    private ScoredEquivalents<Item> merge(ScoredEquivalents<Item> left, ScoredEquivalents<Item> right) {
        HashMap<Publisher, Map<Item, Score>> leftMap = Maps.newHashMap(left.equivalents());
        return null;
    }

    private List<Item> extractGeneratedSuggestions(Iterable<ScoredEquivalents<Item>> generatedScores) {
        return Lists.newArrayList(Iterables.concat(Iterables.transform(generatedScores, new Function<ScoredEquivalents<Item>, Iterable<Item>>() {
            @Override
            public Iterable<Item> apply(ScoredEquivalents<Item> input) {
                return Iterables.concat(Iterables.transform(input.equivalents().values(), new Function<Map<Item, Score>, Iterable<Item>>() {
                    @Override
                    public Iterable<Item> apply(Map<Item, Score> input) {
                        return input.keySet();
                    }
                }));
            }
        })));
    }
    
}
