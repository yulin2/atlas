package org.atlasapi.equiv.scorers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class BroadcastAliasScorerTest {

    private final Score mismatchScore = Score.nullScore();
    private final ContentResolver resolver = mock(ContentResolver.class);
    private final BroadcastAliasScorer scorer = new BroadcastAliasScorer(resolver, mismatchScore);

    @Test
    public void testScoresOneWhenCandidateHasMatchingAlias() {
        Item subj = broadcastWithAlias("AliasTest");

        Item cand = broadcastWithAlias("AliasTest");

        ScoredCandidates<Item> scoredCandidates = scorer.score(subj,
                ImmutableSet.of(cand),
                new DefaultDescription());
        assertThat(scoredCandidates.candidates().get(cand), is(Score.ONE));
    }

    @Test
    public void testScoresNullWhenCandidateHasNoMatchingAlias() {
        Item subj = broadcastWithAlias("TestAlias");

        Item cand = broadcastWithAlias("AliasTest");

        ScoredCandidates<Item> scoredCandidates = scorer.score(subj,
                ImmutableSet.of(cand),
                new DefaultDescription());
        assertThat(scoredCandidates.candidates().get(cand), is(Score.nullScore()));
    }

    @Test
    public void testScoresNullWhenNeitherHasAnyAliases() {
        Item subj = broadcastWithNoAlias();

        Item cand = broadcastWithNoAlias();

        ScoredCandidates<Item> scoredCandidates = scorer.score(subj,
                ImmutableSet.of(cand),
                new DefaultDescription());
        assertThat(scoredCandidates.candidates().get(cand), is(Score.nullScore()));
    }

    private Item broadcastWithAlias(String alias) {
        Item item = new Item("item", "item", Publisher.METABROADCAST);
        Version version = new Version();
        Broadcast broadcast = new Broadcast("broadcast", DateTime.now(), DateTime.now()
                .plusHours(1));
        broadcast.addAliasUrl(alias);
        version.addBroadcast(broadcast);
        item.addVersion(version);
        return item;
    }

    private Item broadcastWithNoAlias() {
        Item item = new Item("item", "item", Publisher.METABROADCAST);
        Version version = new Version();
        Broadcast broadcast = new Broadcast("broadcast", DateTime.now(), DateTime.now()
                .plusHours(1));
        version.addBroadcast(broadcast);
        item.addVersion(version);
        return item;
    }
}