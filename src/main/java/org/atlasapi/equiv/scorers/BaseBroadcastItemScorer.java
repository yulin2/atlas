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
 * <p>Specialized {@link EquivalenceScorer} for "broadcast items", i.e. those that
 * have exactly one {@link org.atlasapi.media.entity.Broadcast Broadcast} per
 * {@link Item} rather than many.</p>
 * 
 * For each candidate, it will check for matches, in order:
 * <ol>
 * <li>The subject against the candidate</li>
 * <li>The subject's {@link Container} against the candidate</li>
 * <li>The candidate's Container against the subject.
 * </ol>
 * 
 * <p>Subclasses must implement each of the above 3 checks.</p> 
 * 
 * <p>Any match results in a score of {@link Score#ONE one} otherwise the score is
 * configured mismatch score.</p>
 */
public abstract class BaseBroadcastItemScorer implements EquivalenceScorer<Item> {
    
    private final ContentResolver resolver;
    private final Score misMatchScore;
    
    public BaseBroadcastItemScorer(ContentResolver resolver, Score misMatchScore) {
        this.resolver = checkNotNull(resolver);
        this.misMatchScore = checkNotNull(misMatchScore);
    }

    /**
     * @inheritDoc
     */
    @Override
    public final ScoredCandidates<Item> score(Item subject, Set<? extends Item> candidates,
            ResultDescription desc) {
        Builder<Item> equivalents = DefaultScoredCandidates.fromSource(getName());

        Optional<Container> subjectContainer = getContainerIfHasTitle(subject);
        
        for (Item candidate : candidates) {
            equivalents.addEquivalent(candidate, score(subject, subjectContainer, candidate, desc));
        }

        return equivalents.build();
    }

    /**
     * Provide the unique identifier for this scorer.
     * @return the name of this scorer
     */
    protected abstract String getName();

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
        
        if (subjectAndCandidateMatch(subject, candidate)) {
            desc.appendText("%s scores %s through subject",  uriAndTitle(candidate), Score.ONE); 
            return Score.ONE;
        }
        
        if (subjectContainer.isPresent()
                && subjectContainerAndCandidateMatch(subjectContainer.get(), candidate)) {
            desc.appendText("%s scores %s through subject container %s",
                    uriAndTitle(candidate), Score.ONE, uriAndTitle(subjectContainer.get())); 
            return Score.ONE;
        }
        
        Optional<Container> candidateContainer = getContainerIfHasTitle(candidate);
        
        if (candidateContainer.isPresent()
                && subjectAndCandidateContainerMatch(subject, candidateContainer.get())) {
            desc.appendText("%s scores %s through candidate container %s",
                    uriAndTitle(candidate), Score.ONE, uriAndTitle(candidateContainer.get())); 
            return Score.ONE;
        }
        
        desc.appendText("%s scores %s, no item/container title matches",
                candidate.getCanonicalUri(), misMatchScore); 
        return misMatchScore;
    }
    
    private String uriAndTitle(Content c) {
        return String.format("'%s' (%s)", c.getTitle(), c.getCanonicalUri());
    }
    
    /**
     * Check if there is a match between the subject and candidate.
     * @param subject - the subject of the update.
     * @param candidate - the candidate being scored.
     * @return true if there is the subject and candidate match, false otherwise.
     */
    protected abstract boolean subjectAndCandidateMatch(Item subject, Item candidate);

    /**
     * Check if there is a match between the subject and candidate.
     * @param subject - the subject of the update
     * @param candidateContainer - the container of the candidate being scored.
     * @return true if there is the subject and candidate container match, false otherwise.
     */
    protected abstract boolean subjectAndCandidateContainerMatch(Item subject, Container candidateContainer);


    /**
     * Check if there is a match between the subject and candidate.
     * @param subjectContainer
     * @param candidate
     * @return true if there is the subject container and candidate container match, false otherwise.
     */
    protected abstract boolean subjectContainerAndCandidateMatch(Container subjectContainer, Item candidate);

    @Override
    public String toString() {
        return getName();
    }
    
}
