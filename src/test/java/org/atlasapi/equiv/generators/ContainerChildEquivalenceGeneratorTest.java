package org.atlasapi.equiv.generators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(JMock.class)
public class ContainerChildEquivalenceGeneratorTest extends TestCase {

    private final String A_SORT_KEY = "asdf";
    private final DateTime NULL_UPDATED = null;

    private final Mockery context = new Mockery();
    @SuppressWarnings("unchecked")
    private final EquivalenceUpdater<Item> itemUpdater = context.mock(EquivalenceUpdater.class);
    private final ContentResolver contentResolver = context.mock(ContentResolver.class);
    private final LiveEquivalenceResultStore resultStore = context.mock(LiveEquivalenceResultStore.class);
    
    private final ContainerChildEquivalenceGenerator generator = new ContainerChildEquivalenceGenerator(contentResolver, itemUpdater, resultStore);
    
    @Test
    public void testAttemptsToResolveAllChildrenOfContainer() {
        
        final ImmutableList<ChildRef> childRefs = ImmutableList.of(
                new ChildRef("child1", A_SORT_KEY, NULL_UPDATED, EntityType.EPISODE),
                new ChildRef("child2", A_SORT_KEY, NULL_UPDATED, EntityType.EPISODE)
        );
        
        context.checking(new Expectations(){{
            ignoring(itemUpdater);
            ignoring(resultStore);
            one(contentResolver).findByCanonicalUris(Lists.transform(childRefs, ChildRef.TO_URI));
                will(returnValue(ResolvedContent.builder().build()));
        }});

        Container container = new Container();
        container.setChildRefs(childRefs);
        
        generator.generate(container, new DefaultDescription());
    }
    
    @Test
    public void testCallsUpdaterAndStoresResultForChildrenOfContainer() {
        
        final Episode ep = new Episode("ep1", "cep2", Publisher.BBC);
        
        final ImmutableList<ChildRef> childRefs = ImmutableList.of(ep.childRef());

        final EquivalenceResult<Item> equivResult = resultFor(ep, ImmutableMap.<Publisher,ScoredCandidate<Item>>of());

        context.checking(new Expectations(){{
            one(contentResolver).findByCanonicalUris(Lists.transform(childRefs, ChildRef.TO_URI));
                will(returnValue(ResolvedContent.builder().put(ep.getCanonicalUri(), ep).build()));
            one(itemUpdater).updateEquivalences(ep, Optional.<List<Item>>absent());
                will(returnValue(equivResult));
            one(resultStore).store(equivResult);will(returnValue(equivResult));
        }});
        
        Container container = new Container();
        container.setChildRefs(childRefs);
        
        generator.generate(container, new DefaultDescription());
    }
    
    @Test
    public void testExtractsContainerFromStrongItemEquivalents() {
        
        final Episode ep = new Episode("ep1", "cep2", Publisher.BBC);
        
        final ImmutableList<ChildRef> childRefs = ImmutableList.of(ep.childRef());

        final String equivParentUri = "containerUri";
        final Container equivParent = new Brand(equivParentUri, equivParentUri, Publisher.PA);
        
        final Episode equiv = new Episode("equiv","cequiv",Publisher.PA);
        equiv.setContainer(equivParent);
        equivParent.setChildRefs(ImmutableList.of(equiv.childRef()));
        
        final EquivalenceResult<Item> equivResult = resultFor(ep, ImmutableMap.of(Publisher.PA, ScoredCandidate.<Item>valueOf(equiv, Score.ONE)));

        context.checking(new Expectations(){{
            one(contentResolver);
                will(returnValue(ResolvedContent.builder().put(ep.getCanonicalUri(), ep).build()));
            one(itemUpdater).updateEquivalences(ep, Optional.<List<Item>>absent());
                will(returnValue(equivResult));
            one(resultStore).store(equivResult);will(returnValue(equivResult));
            one(contentResolver);
                will(returnValue(ResolvedContent.builder().put(equivParentUri,equivParent).build()));
        }});
        
        Container container = new Container();
        container.setChildRefs(childRefs);
        
        ScoredCandidates<Container> scores = generator.generate(container, new DefaultDescription());
        
        assertThat(scores.candidates(), hasEntry(equivParent, Score.ONE));
    }

    private EquivalenceResult<Item> resultFor(final Episode ep, Map<Publisher, ScoredCandidate<Item>> strong) {
        return new EquivalenceResult<Item>(ep, ImmutableList.<ScoredCandidates<Item>> of(), null, strong, null);
    }
}
