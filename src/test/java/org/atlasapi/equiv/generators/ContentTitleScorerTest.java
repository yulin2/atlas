package org.atlasapi.equiv.generators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ContentTitleScorerTest {

    private final ContentTitleScorer<Container> scorer = new ContentTitleScorer<Container>();
    private final ResultDescription desc = new DefaultDescription();

    @Test
    public void testScore() {
        scoreLt(0.01, scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("nothing")), desc));
        score(1, scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("biker")), desc));
        scoreLt(0.1, scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("bike")), desc));
        scoreLt(0.1, scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("bikers")), desc));
    }

    @Test
    public void testScoreSymmetry() {
        assertEquals(scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("biker")), desc), scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("biker")), desc));
        assertEquals(scorer.scoreCandidates(brandWithTitle("bike"), ImmutableList.of(brandWithTitle("biker")), desc), scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("bike")), desc));
        assertEquals(scorer.scoreCandidates(brandWithTitle("bikers"), ImmutableList.of(brandWithTitle("biker")), desc), scorer.scoreCandidates(brandWithTitle("biker"), ImmutableList.of(brandWithTitle("bikers")), desc));
    }
    
    
    @Test
    public void testScoreWithAmpersands() {
        score(1, scorer.scoreCandidates(brandWithTitle("Rosencrantz & Guildenstern Are Dead"), ImmutableList.of(brandWithTitle("Rosencrantz and Guildenstern Are Dead")), desc));
        score(1, scorer.scoreCandidates(brandWithTitle("Bill & Ben"), ImmutableList.of(brandWithTitle("Bill and Ben")), desc));
    }
    
    @Test
    public void testScoreWithCommonPrefix() {
        score(1, scorer.scoreCandidates(brandWithTitle("The Great Escape"), ImmutableList.of(brandWithTitle("The Great Escape")), desc));
        scoreLt(0.1, scorer.scoreCandidates(brandWithTitle("The Great Escape"), ImmutableList.of(brandWithTitle("Italian Job")), desc));
        
        score(1, scorer.scoreCandidates(brandWithTitle("The Great Escape"), ImmutableList.of(brandWithTitle("Great Escape")), desc));
        score(1, scorer.scoreCandidates(brandWithTitle("the Great Escape"), ImmutableList.of(brandWithTitle("Great Escape")), desc));
        scoreLt(0.1, scorer.scoreCandidates(brandWithTitle("Theatreland"), ImmutableList.of(brandWithTitle("The atreland")), desc));
        scoreLt(0.1, scorer.scoreCandidates(brandWithTitle("theatreland"), ImmutableList.of(brandWithTitle("the atreland")), desc));
    }
    
    private void scoreLt(double expected, ScoredCandidates<Container> candidates) {
        Score score = scoredOfOnly(candidates);
        assertThat(score.asDouble(), is(lessThan(expected)));
    }

    private Score scoredOfOnly(ScoredCandidates<Container> candidates) {
        return Iterables.getOnlyElement(candidates.candidates().entrySet()).getValue();
    }

    private void score(double expected, ScoredCandidates<Container> candidates) {
        Score score = scoredOfOnly(candidates);
        assertTrue(String.format("expected %s got %s", expected, score), score.equals(expected > 0 ? Score.valueOf(expected) : Score.NULL_SCORE));
    }

    private Container brandWithTitle(String title) {
        Brand brand = new Brand("uri","curie",Publisher.BBC);
        brand.setTitle(title);
        return brand;
    }


}
