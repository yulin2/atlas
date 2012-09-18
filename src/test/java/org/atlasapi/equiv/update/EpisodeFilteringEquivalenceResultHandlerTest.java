package org.atlasapi.equiv.update;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.ContentRef;
import org.atlasapi.equiv.EquivalenceSummary;
import org.atlasapi.equiv.EquivalenceSummaryStore;
import org.atlasapi.equiv.handlers.EpisodeFilteringEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeFilteringEquivalenceResultHandlerTest {

    @SuppressWarnings("unchecked")
    private final EquivalenceResultHandler<Item> delegate = mock(EquivalenceResultHandler.class);
    private final EquivalenceSummaryStore summaryStore = mock(EquivalenceSummaryStore.class);
    
    private Episode subject;
    private Brand subjectContainer;
    
    private final List<ScoredCandidates<Item>> noScores = ImmutableList.of();
    private final ScoredCandidates<Item> emptyCombined = DefaultScoredCandidates.fromMappedEquivs("test", ImmutableMap.<Item, Score>of());
    
    @Before
    public void setup() {
        subject = new Episode("episode","episodeCurie", Publisher.PA);
        subject.setId(1);
        subject.setThisOrChildLastUpdated(new DateTime(DateTimeZones.UTC));
        
        subjectContainer = new Brand("brand", "brandCurie", Publisher.PA);
        subjectContainer.setId(2);
        
        subject.setContainer(subjectContainer);
    }

    private Matcher<EquivalenceResult<Item>> resultWithStrongEquiv(final Publisher publisher, final String uri) {
        return new TypeSafeMatcher<EquivalenceResult<Item>>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText("result with strong equivalent: ")
                    .appendValue(publisher)
                    .appendText("/")
                    .appendValue(uri);
            }

            @Override
            public boolean matchesSafely(EquivalenceResult<Item> result) {
                return result.strongEquivalences().get(publisher).candidate().getCanonicalUri().equals(uri);
            }
        };
    }

    private Matcher<EquivalenceResult<Item>> resultWithNoStrongEquivalents() {
        return new TypeSafeMatcher<EquivalenceResult<Item>>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText("result with no strong equivalences");
            }

            @Override
            public boolean matchesSafely(EquivalenceResult<Item> result) {
                return result.strongEquivalences().isEmpty();
            }
        };
    }
    
    @Test
    public void testFiltersItemFromNonStrongBrand() {
        
        Container strongContainer = new Brand("pabrand", "pabrandCurie", Publisher.PA);
        strongContainer.setId(3);

        EquivalenceSummary equivSummary = summary(subjectContainer.getId(), strongContainer);
        
        when(summaryStore.summariesForIds(argThat(hasItem(subject.getContainer().getId()))))
            .thenReturn(ImmutableOptionalMap.copyOf(ImmutableMap.of(subject.getContainer().getId(), Optional.of(equivSummary))));
        
        Episode badEquiv = new Episode("bequiv", "bequivCurie", Publisher.PA);
        badEquiv.setParentRef(new ParentRef(Id.valueOf(4), EntityType.BRAND));
        
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
            Publisher.PA, ScoredCandidate.<Item>valueOf(badEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(subject, noScores, emptyCombined , strong, new DefaultDescription());

        EquivalenceResultHandler<Item> handler = EpisodeFilteringEquivalenceResultHandler.relaxed(delegate, summaryStore);
        
        handler.handle(result);
        
        verify(delegate).handle(argThat(resultWithNoStrongEquivalents()));
    }
    
    
    @Test
    public void testDoesntFilterItemFromStrongBrand() {
        
        Container strongContainer = new Brand("bbcbrand", "bbcbrandCurie", Publisher.BBC);

        EquivalenceSummary equivSummary = summary(subjectContainer.getId(), strongContainer);
        
        when(summaryStore.summariesForIds(argThat(hasItem(subject.getContainer().getId()))))
            .thenReturn(ImmutableOptionalMap.copyOf(ImmutableMap.of(subject.getContainer().getId(), Optional.of(equivSummary))));
        
        Episode goodEquiv = new Episode("gequiv", "gequivCurie", Publisher.BBC);
        goodEquiv.setContainer(strongContainer);
        
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
            Publisher.BBC, ScoredCandidate.<Item>valueOf(goodEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(subject, noScores, emptyCombined , strong, new DefaultDescription());
        
        EquivalenceResultHandler<Item> handler = EpisodeFilteringEquivalenceResultHandler.relaxed(delegate, summaryStore);

        handler.handle(result);

        verify(delegate).handle(argThat(resultWithStrongEquiv(Publisher.BBC, "gequiv")));
    }
    
    @Test
    public void testDoesntFilterItemFromSourceWithNoStrongBrandsWhenRelaxed() {

        EquivalenceSummary equivSummary = new EquivalenceSummary(subject.getContainer().getId(), ImmutableList.<Id>of(), ImmutableMap.<Publisher,ContentRef>of());
        
        when(summaryStore.summariesForIds(argThat(hasItem(subject.getContainer().getId()))))
            .thenReturn(ImmutableOptionalMap.fromMap(ImmutableMap.of(subject.getContainer().getId(), equivSummary)));
        
        Episode ignoredEquiv = new Episode("ignoredequiv", "ignoredequiv", Publisher.C4);
        ignoredEquiv.setParentRef(new ParentRef(Id.valueOf(4), EntityType.BRAND));

        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
            Publisher.C4, ScoredCandidate.<Item>valueOf(ignoredEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(subject, noScores, emptyCombined , strong, new DefaultDescription());

        EquivalenceResultHandler<Item> handler = EpisodeFilteringEquivalenceResultHandler.relaxed(delegate, summaryStore);
        
        handler.handle(result);

        verify(delegate).handle(argThat(resultWithStrongEquiv(Publisher.C4, "ignoredequiv")));
    }
    
    @Test
    public void testDoesntFilterItemWithNoBrand() {
        
        EquivalenceSummary equivSummary = new EquivalenceSummary(subject.getId(), ImmutableList.<Id>of(), ImmutableMap.<Publisher,ContentRef>of());
        
        when(summaryStore.summariesForIds(argThat(hasItem(subject.getContainer().getId()))))
            .thenReturn(ImmutableOptionalMap.copyOf(ImmutableMap.of(subject.getContainer().getId(), Optional.of(equivSummary))));
        
        Item noBrand = new Item("nobrand", "nobrandCurie", Publisher.FIVE);
        
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
                Publisher.FIVE, ScoredCandidate.<Item>valueOf(noBrand, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(subject, noScores, emptyCombined , strong, new DefaultDescription());

        EquivalenceResultHandler<Item> handler = EpisodeFilteringEquivalenceResultHandler.relaxed(delegate, summaryStore);
        
        handler.handle(result);
        
        verify(delegate).handle(argThat(resultWithStrongEquiv(Publisher.FIVE, "nobrand")));
    }
    
    @Test
    public void testFiltersItemFromSourceWithNoStrongBrandsWhenStrict() {
        
        EquivalenceSummary equivSummary = new EquivalenceSummary(
            subject.getContainer().getId(), 
            ImmutableList.<Id>of(), 
            ImmutableMap.<Publisher,ContentRef>of()
        );
        
        when(summaryStore.summariesForIds(argThat(hasItem(subject.getContainer().getId()))))
            .thenReturn(ImmutableOptionalMap.fromMap(ImmutableMap.of(
                subject.getContainer().getId(), equivSummary
            )));
        
        Episode ignoredEquiv = new Episode("filteredequiv", "filteredequiv", Publisher.C4);
        ignoredEquiv.setParentRef(new ParentRef(Id.valueOf(4), EntityType.BRAND));

        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
            Publisher.C4, ScoredCandidate.<Item>valueOf(ignoredEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(
            subject, noScores, emptyCombined , strong, new DefaultDescription()
        );

        EquivalenceResultHandler<Item> handler = EpisodeFilteringEquivalenceResultHandler.strict(delegate, summaryStore);
        
        handler.handle(result);

        verify(delegate).handle(argThat(resultWithNoStrongEquivalents()));
    }

    private EquivalenceSummary summary(Id id, Container strongContainer) {
        return new EquivalenceSummary(id, ImmutableList.<Id>of(), ImmutableMap.of(strongContainer.getPublisher(), ContentRef.valueOf(strongContainer)));
    }

}
