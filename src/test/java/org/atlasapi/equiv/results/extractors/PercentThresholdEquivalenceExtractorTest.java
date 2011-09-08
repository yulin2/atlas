package org.atlasapi.equiv.results.extractors;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class PercentThresholdEquivalenceExtractorTest extends TestCase {

    public void testExtractsItemWith90PercentOfTotalWithNegatives() {

        PercentThresholdEquivalenceExtractor<Item> extractor = PercentThresholdEquivalenceExtractor.<Item>extractorMoreThanPercent(90);
        
        ScoredEquivalent<Item> strong = ScoredEquivalent.equivalentScore(new Item("test1","cur1",Publisher.BBC), Score.valueOf(0.5));
        Maybe<ScoredEquivalent<Item>> extract = extractor.extract(null, ImmutableList.<ScoredEquivalent<Item>>of(
                strong,
                ScoredEquivalent.equivalentScore(new Item("test2","cur2",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredEquivalent.equivalentScore(new Item("test3","cur3",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredEquivalent.equivalentScore(new Item("test4","cur4",Publisher.BBC), Score.valueOf(-0.5))
        ), new DefaultDescription());
        
        assertTrue("Nothing strong extracted", extract.hasValue());
        assertEquals(extract.requireValue(), strong);
        
    }
    
    public void testDoesntExtractItemWhenAllNegative() {

        PercentThresholdEquivalenceExtractor<Item> extractor = PercentThresholdEquivalenceExtractor.<Item>extractorMoreThanPercent(90);
        
        Maybe<ScoredEquivalent<Item>> extract = extractor.extract(null, ImmutableList.<ScoredEquivalent<Item>>of(
                ScoredEquivalent.equivalentScore(new Item("test1","cur1",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredEquivalent.equivalentScore(new Item("test2","cur2",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredEquivalent.equivalentScore(new Item("test3","cur3",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredEquivalent.equivalentScore(new Item("test4","cur4",Publisher.BBC), Score.valueOf(-0.5))
        ), new DefaultDescription());
        
        assertTrue("Something strong extracted", extract.isNothing());
    }

}
