package org.atlasapi.equiv.results.combining;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;

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
        
        assertEquals(Score.valueOf(5.0), combined.equivalents().get(equivalent3));
        
        assertEquals(Score.valueOf(7.5), combined.equivalents().get(equivalent1));
        assertEquals(Score.valueOf(5.0), combined.equivalents().get(equivalent2));
    }
    
    public void testCombineNulls() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                DefaultScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, Score.NULL_SCORE).addEquivalent(equivalent2, Score.NULL_SCORE).build(),
                DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent2, Score.valueOf(5.0)).addEquivalent(equivalent3, Score.valueOf(5.0)).build(),
                DefaultScoredEquivalents.<Item>fromSource("source3").addEquivalent(equivalent3, Score.NULL_SCORE).build()
        );
        
        ScoredEquivalents<Item> combined = combiner.combine(scores);

        assertEquals(Score.valueOf(5.0), combined.equivalents().get(equivalent3));

        assertEquals(Score.NULL_SCORE,   combined.equivalents().get(equivalent1));
        assertEquals(Score.valueOf(5.0), combined.equivalents().get(equivalent2));
        
    }

}
