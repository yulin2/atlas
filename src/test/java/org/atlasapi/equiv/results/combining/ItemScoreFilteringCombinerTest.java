package org.atlasapi.equiv.results.combining;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoreThreshold;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ItemScoreFilteringCombinerTest extends MockObjectTestCase {

    public final Item target = target("target", "Target", Publisher.BBC);
    
    private Item target(String name, String title, Publisher publisher) {
        Item target = new Item(name+"Uri", name+"Curie", publisher);
        target.setTitle("Test " + title);
        return target;
    }
    
    @SuppressWarnings("unchecked")
    private final EquivalenceCombiner<Item> delegate = mock(EquivalenceCombiner.class);
    private final ResultDescription desc = mock(ResultDescription.class);
    
    private final String sourceName = "itemSource";
    private final ItemScoreFilteringCombiner<Item> combiner = new ItemScoreFilteringCombiner<Item>(delegate, sourceName, ScoreThreshold.greaterThan(0.2));
    
    public void testMaintainsScoreWhenItemScorePassesThreshold() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.of(target, Score.valueOf(0.8))));

        final Score combinedScore = Score.ONE;
        
        checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, combinedScore))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, combinedScore));
        
    }
    
    public void testSetsScoreToNullWhenItemScoreFailsThreshold() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.of(target, Score.valueOf(0.1))));

        checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.NULL_SCORE));
        
    }
    
    public void testSetsScoreToNullWhenItemScoreIsNull() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.of(target, Score.NULL_SCORE)));

        checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.NULL_SCORE));
        
    }

    public void testSetsScoreToNullWhenItemScoreIsMissing() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.<Item, Score>of()));

        checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.NULL_SCORE));
        
    }

    public void testDoesntFilterScoresWhenItemSourceNotFound() {
        
        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs("anotherSource", ImmutableMap.of(target, Score.valueOf(0.1))));

        checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.ONE));
        
    }
    
    private ScoredEquivalents<Item> scoredEquivs(String source, Map<Item, Score> scoreMap) {
        return DefaultScoredEquivalents.fromMappedEquivs(source, scoreMap);
    }

}
