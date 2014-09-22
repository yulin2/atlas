package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.results.scores.Score;
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
    private static final Function<Broadcast, Iterable<String>> BROADCAST_TO_ALIAS_STRING_ITERABLE = new Function<Broadcast, Iterable<String>>() {

        @Override
        public Iterable<String> apply(Broadcast input) {
            return input.getAliasUrls();
        }

    };

    public BroadcastAliasScorer(ContentResolver resolver, Score misMatchScore) {
        super(resolver, misMatchScore);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    protected boolean subjectAndCandidateMatch(Item subject, Item candidate) {

        ImmutableSet<String> aliasesOfCandidateBroadcasts = FluentIterable.from(candidate.flattenBroadcasts())
                .transformAndConcat(BROADCAST_TO_ALIAS_STRING_ITERABLE)
                .toSet();

        ImmutableSet<String> aliasesOfSubjectBroadcasts = FluentIterable.from(subject.flattenBroadcasts())
                .transformAndConcat(BROADCAST_TO_ALIAS_STRING_ITERABLE)
                .toSet();

        return !Sets.intersection(aliasesOfCandidateBroadcasts, aliasesOfSubjectBroadcasts).isEmpty();
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