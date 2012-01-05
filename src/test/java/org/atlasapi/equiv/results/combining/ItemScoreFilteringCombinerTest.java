package org.atlasapi.equiv.results.combining;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoreThreshold;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(JMock.class)
public class ItemScoreFilteringCombinerTest extends TestCase {

    public final Item target = target("target", "Target", Publisher.BBC);
    
    private Item target(String name, String title, Publisher publisher) {
        Item target = new Item(name+"Uri", name+"Curie", publisher);
        target.setTitle("Test " + title);
        return target;
    }

    private final Mockery context = new Mockery();
    @SuppressWarnings("unchecked")
    private final EquivalenceCombiner<Item> delegate = context.mock(EquivalenceCombiner.class);
    private final ResultDescription desc = context.mock(ResultDescription.class);
    
    private final String sourceName = "itemSource";
    private final ItemScoreFilteringCombiner<Item> combiner = new ItemScoreFilteringCombiner<Item>(delegate, sourceName, ScoreThreshold.greaterThan(0.2));

    @Test
    public void testMaintainsScoreWhenItemScorePassesThreshold() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.of(target, Score.valueOf(0.8))));

        final Score combinedScore = Score.ONE;
        
        context.checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, combinedScore))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, combinedScore));
        
    }

    @Test
    public void testSetsScoreToNullWhenItemScoreFailsThreshold() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.of(target, Score.valueOf(0.1))));

        context.checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.NULL_SCORE));
        
    }

    @Test
    public void testSetsScoreToNullWhenItemScoreIsNull() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.of(target, Score.NULL_SCORE)));

        context.checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.NULL_SCORE));
        
    }

    @Test
    public void testSetsScoreToNullWhenItemScoreIsMissing() {

        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs(sourceName, ImmutableMap.<Item, Score>of()));

        context.checking(new Expectations(){{
            ignoring(desc);
            one(delegate).combine(scores, desc); 
                will(returnValue(scoredEquivs("asource", ImmutableMap.of(target, Score.ONE))));
        }});
        
        ScoredEquivalents<Item> combined = combiner.combine(scores, desc);
        
        assertThat(combined.equivalents(), hasEntry(target, Score.NULL_SCORE));
        
    }

    @Test
    public void testDoesntFilterScoresWhenItemSourceNotFound() {
        
        final List<ScoredEquivalents<Item>> scores = ImmutableList.of(scoredEquivs("anotherSource", ImmutableMap.of(target, Score.valueOf(0.1))));

        context.checking(new Expectations(){{
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
