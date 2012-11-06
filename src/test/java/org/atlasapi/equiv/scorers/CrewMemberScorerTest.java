package org.atlasapi.equiv.scorers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class CrewMemberScorerTest {

    private final CrewMemberScorer scorer = new CrewMemberScorer();
    
    private final ResultDescription desc = new DefaultDescription();
    
    @Test
    public void testScoresNullWithNoCrewMembers() {
        assertThat(scoreFrom(score(itemWithCrew(), itemWithCrew())), is(Score.NULL_SCORE));
    }
    
    @Test
    public void testScoresOneWhenAllCrewMatch() {
        assertThat(scoreFrom(score(
            itemWithCrew(crew("Romeo")), 
            itemWithCrew(crew("Romeo"))
        )), is(Score.ONE));
    }

    @Test
    public void testScoresNegativeWhenNoCrewMatch() {
        assertThat(scoreFrom(score(
            itemWithCrew(crew("Romeo")), 
            itemWithCrew(crew("MC Harvey"))
        )), is(Score.valueOf(-1.0)));
    }

    @Test
    public void testScoresZeroWhenHalfCrewMatch() {
        assertThat(scoreFrom(score(
            itemWithCrew(crew("Romeo"), crew("Carl Morgan")), 
            itemWithCrew(crew("Romeo"), crew("Lisa Maffia"))
        )), is(Score.valueOf(0.0)));
    }

    @Test
    public void testScoresSymmetrically() {
        assertThat(scoreFrom(score(
            itemWithCrew(crew("Romeo")), 
            itemWithCrew(crew("Romeo"), crew("Lisa Maffia"))
        )), is(scoreFrom(score(
            itemWithCrew(crew("Romeo"), crew("Lisa Maffia")),
            itemWithCrew(crew("Romeo"))
        ))));
    }

    private CrewMember crew(String name) {
        return new CrewMember().withName(name);
    }

    protected ScoredCandidates<Item> score(Item subject, Item candidate) {
        return scorer.score(subject, ImmutableSet.of(candidate), desc);
    }

    protected Score scoreFrom(ScoredCandidates<Item> scored) {
        return Iterables.getOnlyElement(scored.candidates().values());
    }

    private Item itemWithCrew(CrewMember...crew) {
        Item item = new Item();
        item.setPeople(ImmutableList.copyOf(crew));
        return item;
    }

}
