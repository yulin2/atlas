package org.atlasapi.equiv.generators;

import static com.google.common.collect.ImmutableSet.of;
import junit.framework.TestCase;

import org.atlasapi.equiv.generators.TitleMatchingItemEquivalenceScorer.TitleType;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.Iterables;

public class TitleMatchingItemEquivalenceScorerTest extends TestCase {
    
    
    public void testTitleTyping() {
        
       assertEquals(TitleType.DATE, TitleType.titleTypeOf(itemWithTitle("09/10/2011")));
       assertEquals(TitleType.DATE, TitleType.titleTypeOf(itemWithTitle("9/10/2011")));
       assertEquals(TitleType.DATE, TitleType.titleTypeOf(itemWithTitle("09/1/2011")));
       assertEquals(TitleType.DATE, TitleType.titleTypeOf(itemWithTitle("1/1/2011")));
       assertEquals(TitleType.DATE, TitleType.titleTypeOf(itemWithTitle("1/1/11")));
       
       assertEquals(TitleType.SEQUENCE, TitleType.titleTypeOf(itemWithTitle("Episode 1")));
       assertEquals(TitleType.SEQUENCE, TitleType.titleTypeOf(itemWithTitle("Episode: 1")));
       assertEquals(TitleType.SEQUENCE, TitleType.titleTypeOf(itemWithTitle("Episode - 1")));
       assertEquals(TitleType.SEQUENCE, TitleType.titleTypeOf(itemWithTitle("episode  1")));
       assertEquals(TitleType.SEQUENCE, TitleType.titleTypeOf(itemWithTitle("episode 14")));
       
       assertEquals(TitleType.DEFAULT, TitleType.titleTypeOf(itemWithTitle("09/10/20118")));
       assertEquals(TitleType.DEFAULT, TitleType.titleTypeOf(itemWithTitle("009/10/2011")));
       assertEquals(TitleType.DEFAULT, TitleType.titleTypeOf(itemWithTitle("09/100/2011")));
        
    }

    public void testGenerateEquivalences() {

        TitleMatchingItemEquivalenceScorer scorer = new TitleMatchingItemEquivalenceScorer();
        
        score(1.0, scorer.score(itemWithTitle("09/10/2011"), of(itemWithTitle("09/10/2011"))));
        
        score(0, scorer.score(itemWithTitle("19/10/2011"), of(itemWithTitle("09/10/2011"))));
        score(0, scorer.score(itemWithTitle("Countdown"), of(itemWithTitle("Out of Time"))));
        score(0, scorer.score(itemWithTitle("Episode: 3"), of(itemWithTitle("Episode 5"))));
        
        score(0, scorer.score(itemWithTitle("19/10/2011"), of(itemWithTitle("Different"))));
        score(0, scorer.score(itemWithTitle("Episode 1"), of(itemWithTitle("19/10/2011"))));
        score(0, scorer.score(itemWithTitle("Episode 1"), of(itemWithTitle("Different"))));
        
    }
    
    public void testSeqTitleTypes() {

        TitleMatchingItemEquivalenceScorer scorer = new TitleMatchingItemEquivalenceScorer();
        
        score(1, scorer.score(itemWithTitle("Kinross"), of(itemWithTitle("2. Kinross"))));
        score(1, scorer.score(itemWithTitle("Kinross"), of(itemWithTitle("2: Kinross"))));
        score(1, scorer.score(itemWithTitle("Kinross"), of(itemWithTitle("2 - Kinross"))));
        score(0, scorer.score(itemWithTitle("Kinross"), of(itemWithTitle("2. Different"))));
        
    }

    private void score(double expected, ScoredEquivalents<Item> scores) {
        Score value = Iterables.getOnlyElement(scores.equivalents().entrySet()).getValue();
        assertTrue(String.format("expected %s got %s", expected, value), value.equals(expected > 0 ? Score.valueOf(expected) : Score.NULL_SCORE));
    }

    private Item itemWithTitle(String title) {
        Item item = new Item("uri","curie",Publisher.BBC);
        item.setTitle(title);
        return item;
    }

}
