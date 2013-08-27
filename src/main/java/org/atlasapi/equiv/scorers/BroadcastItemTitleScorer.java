package org.atlasapi.equiv.scorers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates.Builder;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

/**
 * Specialized {@link EquivalenceScorer} for "broadcast items", i.e. those that
 * have exactly one {@link org.atlasapi.media.entity.Broadcast Broadcast} per
 * {@link Item} rather than many.
 * 
 * For each candidate, it will check for exact title matches, in order:
 * <ol>
 * <li>The subject against the candidate</li>
 * <li>The subject's {@link Container} against the candidate</li>
 * <li>The candidate's Container against the subject.
 * </ol>
 * 
 * Any match results in a score of {@link Score#ONE one} otherwise the score is
 * {@link Score#NULL_SCORE null}.
 */
public class BroadcastItemTitleScorer implements EquivalenceScorer<Item> {

    public static final String NAME = "Broadcast-Title";
    
    private final ContentResolver resolver;

    public BroadcastItemTitleScorer(ContentResolver resolver) {
        this.resolver = checkNotNull(resolver);
    }

    @Override
    public ScoredCandidates<Item> score(Item subject, Set<? extends Item> candidates,
            ResultDescription desc) {
        Builder<Item> equivalents = DefaultScoredCandidates.fromSource(NAME);

        Optional<Container> subjectContainer = getContainerIfHasTitle(subject);
        
        for (Item candidate : candidates) {
            equivalents.addEquivalent(candidate, score(subject, subjectContainer, candidate, desc));
        }

        return equivalents.build();
    }

    private Optional<Container> getContainerIfHasTitle(Item candidate) {
        Optional<Container> candidateContainer = Optional.absent();
        if (candidate.getContainer() != null) {
            candidateContainer = resolveContainerIfItHasTitle(candidate.getContainer());
        }
        return candidateContainer;
    }

    private Optional<Container> resolveContainerIfItHasTitle(ParentRef containerRef) {
        String containerUri = containerRef.getUri();
        ResolvedContent resolved = resolver.findByCanonicalUris(ImmutableList.of(containerUri));
        Maybe<Identified> possibleContainer = resolved.get(containerUri);
        if (possibleContainer.hasValue() && possibleContainer.requireValue() instanceof Container) {
            Container container = (Container)possibleContainer.requireValue();
            if (!Strings.isNullOrEmpty(container.getTitle())) {
                return Optional.of(container);
            }
        }
        return Optional.absent();
    }

    private Score score(Item subject, Optional<Container> subjectContainer, Item candidate, ResultDescription desc) {
        String subjectItemTitle = subject.getTitle();
        String candidateItemTitle = candidate.getTitle();
        
        if (hasTitle(subject) && equalTitles(subject, candidate)) {
            desc.appendText("%s scores %s through subject",  uriAndTitle(candidate), Score.ONE); 
            return Score.ONE;
        }
        if (subjectContainer.isPresent() && equalTitles(subjectContainer, candidateItemTitle)) {
            desc.appendText("%s scores %s through subject container %s",
                    uriAndTitle(candidate), Score.ONE, uriAndTitle(subjectContainer.get())); 
            return Score.ONE;
        }
        
        Optional<Container> candidateContainer = getContainerIfHasTitle(candidate);
        
        if (candidateContainer.isPresent() && equalTitles(candidateContainer, subjectItemTitle)) {
            desc.appendText("%s scores %s through candidate container %s",
                    uriAndTitle(candidate), Score.ONE, uriAndTitle(candidateContainer.get())); 
            return Score.ONE;
        }
        
        desc.appendText("%s scores %s, no item/container title matches",
                candidate.getCanonicalUri(), Score.NULL_SCORE); 
        return Score.NULL_SCORE;
    }

    private boolean equalTitles(Content c1, Content c2) {
        return c1.getTitle().equals(c2.getTitle());
    }

    private boolean equalTitles(Optional<Container> subjectContainer, String candidateItemTitle) {
        return subjectContainer.get().getTitle().equals(candidateItemTitle);
    }

    private String uriAndTitle(Content c) {
        return String.format("'%s' (%s)", c.getTitle(), c.getCanonicalUri());
    }

    private boolean hasTitle(Content c) {
        return !Strings.isNullOrEmpty(c.getTitle());
    }
    
    @Override
    public String toString() {
        return NAME;
    }
    
}
