package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

public class NullScoreAwareAveragingCombinerTest extends TestCase {
    
    private final NullScoreAwareAveragingCombiner<Item> combiner = new NullScoreAwareAveragingCombiner<Item>();

    public final Item target = target("target", "Target", Publisher.BBC);
    public final Item equivalent1 = target("equivalent1", "Equivalent1", Publisher.BBC);
    public final Item equivalent2 = target("equivalent2", "Equivalent2", Publisher.BBC);
    public final Item equivalent3 = target("equivalent3", "Equivalent3", Publisher.C4);
    
    private Item target(String name, String title, Publisher publisher) {
        Item target = new Item(name+"Uri", name+"Curie", publisher);
        target.setTitle("Test " + title);
        return target;
    }
    
    public void testCombine() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                DefaultScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, Score.valueOf(5.0)).addEquivalent(equivalent2, Score.NULL_SCORE).addEquivalent(equivalent3, Score.valueOf(5.0)).build(),
                DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, Score.valueOf(5.0)).addEquivalent(equivalent2, Score.valueOf(5.0)).addEquivalent(equivalent3, Score.valueOf(5.0)).addEquivalent(equivalent1, Score.valueOf(5.0)).build(),
                DefaultScoredEquivalents.<Item>fromSource("source3").addEquivalent(equivalent3, Score.valueOf(5.0)).addEquivalent(equivalent1, Score.NULL_SCORE).build()
        );
        
        ScoredEquivalents<Item> combined = combiner.combine(scores);
        
        assertEquals(ImmutableMap.of(equivalent3, Score.valueOf(5.0)),combined.equivalents().get(Publisher.C4));
        
        assertEquals(ImmutableMap.of(
                equivalent1, Score.valueOf(7.5),
                equivalent2, Score.valueOf(5.0)
        ), combined.equivalents().get(Publisher.BBC));
    }
    
    public void testCombineNulls() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                DefaultScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, Score.NULL_SCORE).addEquivalent(equivalent2, Score.NULL_SCORE).build(),
                DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent2, Score.valueOf(5.0)).addEquivalent(equivalent3, Score.valueOf(5.0)).build(),
                DefaultScoredEquivalents.<Item>fromSource("source3").addEquivalent(equivalent3, Score.NULL_SCORE).build()
        );
        
        ScoredEquivalents<Item> combined = combiner.combine(scores);
        
        assertEquals(ImmutableMap.of(equivalent3, Score.valueOf(5.0)),combined.equivalents().get(Publisher.C4));
        
        assertEquals(ImmutableMap.of(
                equivalent1, Score.NULL_SCORE,
                equivalent2, Score.valueOf(5.0)
        ), combined.equivalents().get(Publisher.BBC));
        
    }

}
