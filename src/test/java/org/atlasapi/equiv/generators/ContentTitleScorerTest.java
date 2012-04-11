package org.atlasapi.equiv.generators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

public class ContentTitleScorerTest {

    private final ContentTitleScorer scorer = new ContentTitleScorer();

    @Test
    public void testScore() {
        scoreLt(0.01, scorer.score(brandWithTitle("biker"), brandWithTitle("nothing")));
        score(1, scorer.score(brandWithTitle("biker"), brandWithTitle("biker")));
        scoreLt(0.1, scorer.score(brandWithTitle("biker"), brandWithTitle("bike")));
        scoreLt(0.1, scorer.score(brandWithTitle("biker"), brandWithTitle("bikers")));
    }

    @Test
    public void testScoreSymmetry() {
        assertEquals(scorer.score(brandWithTitle("biker"), brandWithTitle("biker")), scorer.score(brandWithTitle("biker"), brandWithTitle("biker")));
        assertEquals(scorer.score(brandWithTitle("bike"), brandWithTitle("biker")), scorer.score(brandWithTitle("biker"), brandWithTitle("bike")));
        assertEquals(scorer.score(brandWithTitle("bikers"), brandWithTitle("biker")), scorer.score(brandWithTitle("biker"), brandWithTitle("bikers")));
    }
    
    
    @Test
    public void testScoreWithAmpersands() {
        score(1, scorer.score(brandWithTitle("Rosencrantz & Guildenstern Are Dead"), brandWithTitle("Rosencrantz and Guildenstern Are Dead")));
        score(1, scorer.score(brandWithTitle("Bill & Ben"), brandWithTitle("Bill and Ben")));
    }
    
    @Test
    public void testScoreWithCommonPrefix() {
        score(1, scorer.score(brandWithTitle("The Great Escape"), brandWithTitle("The Great Escape")));
        scoreLt(0.1, scorer.score(brandWithTitle("The Great Escape"), brandWithTitle("Italian Job")));
        
        score(1, scorer.score(brandWithTitle("The Great Escape"), brandWithTitle("Great Escape")));
        score(1, scorer.score(brandWithTitle("the Great Escape"), brandWithTitle("Great Escape")));
        scoreLt(0.1, scorer.score(brandWithTitle("Theatreland"), brandWithTitle("The atreland")));
        scoreLt(0.1, scorer.score(brandWithTitle("theatreland"), brandWithTitle("the atreland")));
    }
    
    private void scoreLt(double expected, Score score) {
        assertThat(score.asDouble(), is(lessThan(expected)));
    }

    private void score(double expected, Score score) {
        assertTrue(String.format("expected %s got %s", expected, score), score.equals(expected > 0 ? Score.valueOf(expected) : Score.NULL_SCORE));
    }

    private Container brandWithTitle(String title) {
        Brand brand = new Brand("uri","curie",Publisher.BBC);
        brand.setTitle(title);
        return brand;
    }


}
