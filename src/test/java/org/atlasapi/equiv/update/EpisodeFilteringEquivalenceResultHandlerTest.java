package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.handlers.EpisodeFilteringEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@RunWith(JMock.class)
public class EpisodeFilteringEquivalenceResultHandlerTest {

    private final Mockery context = new Mockery();
    private final @SuppressWarnings("unchecked") EquivalenceResultHandler<Item> delegateHandler = context.mock(EquivalenceResultHandler.class);
    
    private final Episode target = new Episode("episode","episodeCurie", Publisher.PA);
    private final List<ScoredCandidates<Item>> scores = ImmutableList.of();
    private final ScoredCandidates<Item> combined = DefaultScoredEquivalents.fromMappedEquivs("test", ImmutableMap.<Item, Score>of());
    
    private final Set<Container> strongContainers = ImmutableSet.<Container>of(
            new Brand("bbcbrand", "bbcbrandCurie", Publisher.BBC),
            new Brand("pabrand", "pabrandCurie", Publisher.PA)
            );

    private Matcher<EquivalenceResult<Item>> resultWithStrongEquiv(final Publisher publisher, final String uri) {
        return new TypeSafeMatcher<EquivalenceResult<Item>>() {

            @Override
            public void describeTo(Description arg0) {
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
            public void describeTo(Description arg0) {
            }

            @Override
            public boolean matchesSafely(EquivalenceResult<Item> result) {
                return result.strongEquivalences().isEmpty();
            }
        };
    }
    
    @Test
    public void testFiltersItemFromNonStrongBrand() {
        
         Set<Container> strongContainers = ImmutableSet.<Container>of(
                new Brand("pabrand", "pabrandCurie", Publisher.PA)
        );

        Episode goodEquiv = new Episode("bequiv", "bequivCurie", Publisher.PA);
        goodEquiv.setParentRef(new ParentRef("weakpabrand"));
        
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
                Publisher.PA, ScoredCandidate.<Item>valueOf(goodEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores, combined , strong, new DefaultDescription());
        
        context.checking(new Expectations(){{
            one(delegateHandler).handle(with(resultWithNoStrongEquivalents()));
        }});
        
        EquivalenceResultHandler<Item> handler = new EpisodeFilteringEquivalenceResultHandler(delegateHandler, strongContainers) ;
        handler.handle(result);
    }
    
    
    @Test
    public void testDoesntFilterItemFromStrongBrand() {
        
        Set<Container> strongContainers = ImmutableSet.<Container>of(
                new Brand("bbcbrand", "bbcbrandCurie", Publisher.BBC)
         );

        Episode goodEquiv = new Episode("gequiv", "gequivCurie", Publisher.BBC);
        goodEquiv.setContainer(Iterables.getOnlyElement(strongContainers));
        
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
                Publisher.BBC, ScoredCandidate.<Item>valueOf(goodEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores, combined , strong, new DefaultDescription());
        
        context.checking(new Expectations(){{
            one(delegateHandler).handle(with(resultWithStrongEquiv(Publisher.BBC, "gequiv")));
        }});
        
        EquivalenceResultHandler<Item> handler = new EpisodeFilteringEquivalenceResultHandler(delegateHandler, strongContainers) ;
        handler.handle(result);
    }
    
    @Test
    public void testDoesntFilterItemFromSourceWithNoStrongBrands() {

        Episode ignoredEquiv = new Episode("ignoredequiv", "ignoredequiv", Publisher.C4);
        ignoredEquiv.setParentRef(new ParentRef("weakbutignoredbrand"));

        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
                Publisher.C4, ScoredCandidate.<Item>valueOf(ignoredEquiv, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores, combined , strong, new DefaultDescription());
        
        context.checking(new Expectations(){{
            one(delegateHandler).handle(with(resultWithStrongEquiv(Publisher.C4, "ignoredequiv")));
        }});
        
        EquivalenceResultHandler<Item> handler = new EpisodeFilteringEquivalenceResultHandler(delegateHandler, strongContainers) ;
        handler.handle(result);
    }
    
    @Test
    public void testDoesntFilterItemWithNoBrand() {
        
        Item noBrand = new Item("nobrand", "nobrandCurie", Publisher.FIVE);
        
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
                Publisher.FIVE, ScoredCandidate.<Item>valueOf(noBrand, Score.ONE)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores, combined , strong, new DefaultDescription());
        
        context.checking(new Expectations(){{
            one(delegateHandler).handle(with(resultWithStrongEquiv(Publisher.FIVE, "nobrand")));
        }});
        
        EquivalenceResultHandler<Item> handler = new EpisodeFilteringEquivalenceResultHandler(delegateHandler, strongContainers) ;
        handler.handle(result);
    }

}
