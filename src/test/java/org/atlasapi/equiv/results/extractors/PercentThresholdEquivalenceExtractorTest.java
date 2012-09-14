package org.atlasapi.equiv.results.extractors;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class PercentThresholdEquivalenceExtractorTest extends TestCase {

    @Test
    public void testExtractsItemWith90PercentOfTotalWithNegatives() {

        PercentThresholdEquivalenceExtractor<Item> extractor = PercentThresholdEquivalenceExtractor.<Item>moreThanPercent(90);
        
        ScoredCandidate<Item> strong = ScoredCandidate.valueOf(new Item("test1","cur1",Publisher.BBC), Score.valueOf(0.5));
        Maybe<ScoredCandidate<Item>> extract = extractor.extract(null, ImmutableList.<ScoredCandidate<Item>>of(
                strong,
                ScoredCandidate.valueOf(new Item("test2","cur2",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredCandidate.valueOf(new Item("test3","cur3",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredCandidate.valueOf(new Item("test4","cur4",Publisher.BBC), Score.valueOf(-0.5))
        ), new DefaultDescription());
        
        assertTrue("Nothing strong extracted", extract.hasValue());
        assertEquals(extract.requireValue(), strong);
        
    }

    @Test
    public void testDoesntExtractItemWhenAllNegative() {

        PercentThresholdEquivalenceExtractor<Item> extractor = PercentThresholdEquivalenceExtractor.<Item>moreThanPercent(90);
        
        Maybe<ScoredCandidate<Item>> extract = extractor.extract(null, ImmutableList.<ScoredCandidate<Item>>of(
                ScoredCandidate.valueOf(new Item("test1","cur1",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredCandidate.valueOf(new Item("test2","cur2",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredCandidate.valueOf(new Item("test3","cur3",Publisher.BBC), Score.valueOf(-0.5)),
                ScoredCandidate.valueOf(new Item("test4","cur4",Publisher.BBC), Score.valueOf(-0.5))
        ), new DefaultDescription());
        
        assertTrue("Something strong extracted", extract.isNothing());
    }

}
