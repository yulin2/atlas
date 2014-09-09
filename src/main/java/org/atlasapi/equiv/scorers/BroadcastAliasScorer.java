package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class BroadcastAliasScorer extends BaseBroadcastItemScorer {

    private static final String NAME = "Broadcast-Alias";

    public BroadcastAliasScorer(ContentResolver resolver, Score misMatchScore) {
        super(resolver, misMatchScore);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected boolean subjectAndCandidateMatch(Item subject, Item candidate) {
        ImmutableSet<Alias> candidateAliases = FluentIterable.from(candidate.flattenBroadcasts())
                .transformAndConcat(new Function<Broadcast, Iterable<Alias>>() {

                    @Override
                    public Iterable<Alias> apply(Broadcast input) {
                        return input.getAliases();
                    }
                })
                .toSet();

        for (Broadcast subjectBroadcast : subject.flattenBroadcasts()) {
            if (!Sets.union(candidateAliases, ImmutableSet.copyOf(subjectBroadcast.getAliases()))
                    .isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean subjectAndCandidateContainerMatch(Item subject, Container candidateContainer) {
        return false;
    }

    @Override
    protected boolean subjectContainerAndCandidateMatch(Container subjectContainer, Item candidate) {
        return false;
    }

}
