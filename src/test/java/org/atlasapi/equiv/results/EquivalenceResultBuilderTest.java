package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.combining.AddingEquivalenceCombiner;
import org.atlasapi.equiv.results.combining.ScoreCombiner;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.TopEquivalenceExtractor;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class EquivalenceResultBuilderTest extends TestCase {

    public void testResultFor() {

        ScoreCombiner<Item> combiner = new AddingEquivalenceCombiner<Item>();
        EquivalenceExtractor<Item> extractor = new TopEquivalenceExtractor<Item>();
        EquivalenceResultBuilder<Item> builder = new DefaultEquivalenceResultBuilder<Item>(combiner, extractor);

        Item item = new Item("testUri","testCurie",Publisher.PA);
        Item bbcItem = new Item("bbcItem", "bbcItemCurie", Publisher.BBC);
        Item c4Item = new Item("c4Item", "c4ItemCurie", Publisher.C4);
        
        List<ScoredCandidates<Item>> equivalents = ImmutableList.of(
                DefaultScoredEquivalents.<Item>fromSource("A Source")
                    .addEquivalent(bbcItem, Score.valueOf(5.0))
                    .addEquivalent(c4Item, Score.valueOf(5.0))
                .build(),
                DefaultScoredEquivalents.<Item>fromSource("B Source")
                    .addEquivalent(bbcItem, Score.valueOf(5.0))
                    .addEquivalent(c4Item, Score.valueOf(5.0))
                .build()
        );

        EquivalenceResult<Item> result = builder.resultFor(item, equivalents , new DefaultDescription());
        
        ScoredCandidates<Item> equivalences = result.combinedEquivalences();
        
        assertEquals("A Source/B Source", equivalences.source());
        assertEquals(ImmutableMap.of(
                bbcItem, Score.valueOf(10.0),
                c4Item, Score.valueOf(10.0)
        ), equivalences.candidates());
        
        Map<Publisher, ScoredCandidate<Item>> strongEquivalences = result.strongEquivalences();
        
        assertEquals(ImmutableMap.of(
                Publisher.BBC, ScoredCandidate.valueOf(bbcItem, Score.valueOf(10.0)),
                Publisher.C4, ScoredCandidate.valueOf(c4Item, Score.valueOf(10.0))
        ), strongEquivalences);
        
        
    }

}
