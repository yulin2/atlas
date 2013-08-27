package org.atlasapi.equiv.scorers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class BroadcastItemTitleScorerTest {

    private final ContentResolver resolver = mock(ContentResolver.class);
    private final BroadcastItemTitleScorer scorer
        = new BroadcastItemTitleScorer(resolver);
    
    @Test
    public void testScoresOneIfBrandTitleMatchesWhenSuggestionHasContainer() {
        
        Item subject = new Item("subj", "subj", Publisher.YOUVIEW);
        subject.setTitle("Coast");
        
        Item suggestion = new Item("sugg", "sugg", Publisher.PA);
        suggestion.setTitle("South Wales");
        
        Brand suggestionBrand = new Brand("suggBrand", "suggBrand", Publisher.PA);
        suggestionBrand.setTitle("Coast");
        suggestion.setContainer(suggestionBrand);
        
        when(resolver.findByCanonicalUris(argThat(hasItem(suggestionBrand.getCanonicalUri()))))
            .thenReturn(ResolvedContent.builder().put(suggestionBrand.getCanonicalUri(), suggestionBrand).build());
        
        ScoredCandidates<Item> results = scorer.score(subject, ImmutableSet.of(suggestion), new DefaultDescription());
        
        assertThat(results.candidates().get(suggestion), is(Score.ONE));
        
    }

    @Test
    public void testScoresOneIfBrandTitleMatchesWhenSubjectHasContainer() {
        
        Item subject = new Item("subj", "subj", Publisher.YOUVIEW);
        subject.setTitle("South Wales");
        
        Brand subjectBrand = new Brand("subjBrand", "subjBrand", Publisher.PA);
        subjectBrand.setTitle("Coast");
        subject.setContainer(subjectBrand);
        
        Item suggestion = new Item("sugg", "sugg", Publisher.PA);
        suggestion.setTitle("Coast");
        
        when(resolver.findByCanonicalUris(argThat(hasItem(subjectBrand.getCanonicalUri()))))
        .thenReturn(ResolvedContent.builder().put(subjectBrand.getCanonicalUri(), subjectBrand).build());
        
        ScoredCandidates<Item> results = scorer.score(subject, ImmutableSet.of(suggestion), new DefaultDescription());
        
        assertThat(results.candidates().get(suggestion), is(Score.ONE));
        
    }

    @Test
    public void testScoresOneIfItemTitleMatchesWhenSuggestionHasContainer() {
        
        Item subject = new Item("subj", "subj", Publisher.YOUVIEW);
        subject.setTitle("Coast");
        
        Item suggestion = new Item("sugg", "sugg", Publisher.PA);
        suggestion.setTitle("Coast");
        
        Brand suggestionBrand = new Brand("suggBrand", "suggBrand", Publisher.PA);
        suggestionBrand.setTitle("Coast");
        suggestion.setContainer(suggestionBrand);
        
        ScoredCandidates<Item> results = scorer.score(subject, ImmutableSet.of(suggestion), new DefaultDescription());
        
        assertThat(results.candidates().get(suggestion), is(Score.ONE));
        
        verify(resolver, never()).findByCanonicalUris(anyIterable());
    }

    @Test
    public void testScoresNullIfNoTitlesMatch() {
        
        Item subject = new Item("subj", "subj", Publisher.YOUVIEW);
        
        Brand subjectBrand = new Brand("subjBrand", "subjBrand", Publisher.PA);
        subject.setContainer(subjectBrand);
        
        Item suggestion = new Item("sugg", "sugg", Publisher.PA);
        
        Brand suggestionBrand = new Brand("suggBrand", "suggBrand", Publisher.PA);
        suggestion.setContainer(suggestionBrand);
        
        when(resolver.findByCanonicalUris(argThat(hasItem(subjectBrand.getCanonicalUri()))))
            .thenReturn(ResolvedContent.builder().put(subjectBrand.getCanonicalUri(), subjectBrand).build());
        when(resolver.findByCanonicalUris(argThat(hasItem(suggestionBrand.getCanonicalUri()))))
            .thenReturn(ResolvedContent.builder().put(suggestionBrand.getCanonicalUri(), suggestionBrand).build());
    
        ScoredCandidates<Item> results = scorer.score(subject, ImmutableSet.of(suggestion), new DefaultDescription());
        
        assertThat(results.candidates().get(suggestion), is(Score.NULL_SCORE));
        
        verify(resolver).findByCanonicalUris(argThat(hasItem(subjectBrand.getCanonicalUri())));
        verify(resolver).findByCanonicalUris(argThat(hasItem(suggestionBrand.getCanonicalUri())));
    }

    @SuppressWarnings("unchecked")
    private Iterable<String> anyIterable() {
        return isA(Iterable.class);
    }

}
