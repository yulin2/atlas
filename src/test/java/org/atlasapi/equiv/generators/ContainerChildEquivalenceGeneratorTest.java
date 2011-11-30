package org.atlasapi.equiv.generators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class ContainerChildEquivalenceGeneratorTest extends MockObjectTestCase {

    private final String A_SORT_KEY = "asdf";
    private final DateTime NULL_UPDATED = null;

    @SuppressWarnings("unchecked")
    private final ContentEquivalenceUpdater<Item> itemUpdater = mock(ContentEquivalenceUpdater.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final LiveEquivalenceResultStore resultStore = mock(LiveEquivalenceResultStore.class);
    
    private final ContainerChildEquivalenceGenerator generator = new ContainerChildEquivalenceGenerator(contentResolver, itemUpdater, resultStore, new NullAdapterLog());
    
    public void testAttemptsToResolveAllChildrenOfContainer() {
        
        final ImmutableList<ChildRef> childRefs = ImmutableList.of(
                new ChildRef("child1", A_SORT_KEY, NULL_UPDATED, EntityType.EPISODE),
                new ChildRef("child2", A_SORT_KEY, NULL_UPDATED, EntityType.EPISODE)
        );
        
        checking(new Expectations(){{
            ignoring(itemUpdater);
            ignoring(resultStore);
            one(contentResolver).findByCanonicalUris(Lists.transform(childRefs, ChildRef.TO_URI));
                will(returnValue(ResolvedContent.builder().build()));
        }});

        Container container = new Container();
        container.setChildRefs(childRefs);
        
        generator.generate(container, new DefaultDescription());
    }
    
    public void testCallsUpdaterAndStoresResultForChildrenOfContainer() {
        
        final Episode ep = new Episode("ep1", "cep2", Publisher.BBC);
        
        final ImmutableList<ChildRef> childRefs = ImmutableList.of(ep.childRef());

        final EquivalenceResult<Item> equivResult = resultFor(ep, ImmutableMap.<Publisher,ScoredEquivalent<Item>>of());

        checking(new Expectations(){{
            one(contentResolver).findByCanonicalUris(Lists.transform(childRefs, ChildRef.TO_URI));
                will(returnValue(ResolvedContent.builder().put(ep.getCanonicalUri(), ep).build()));
            one(itemUpdater).updateEquivalences(ep);
                will(returnValue(equivResult));
            one(resultStore).store(equivResult);will(returnValue(equivResult));
        }});
        
        Container container = new Container();
        container.setChildRefs(childRefs);
        
        generator.generate(container, new DefaultDescription());
    }
    
    public void testExtractsContainerFromStrongItemEquivalents() {
        
        final Episode ep = new Episode("ep1", "cep2", Publisher.BBC);
        
        final ImmutableList<ChildRef> childRefs = ImmutableList.of(ep.childRef());

        final String equivParentUri = "containerUri";
        final Container equivParent = new Brand(equivParentUri, equivParentUri, Publisher.PA);
        
        final Episode equiv = new Episode("equiv","cequiv",Publisher.PA);
        equiv.setContainer(equivParent);
        equivParent.setChildRefs(ImmutableList.of(equiv.childRef()));
        
        final EquivalenceResult<Item> equivResult = resultFor(ep, ImmutableMap.of(Publisher.PA, ScoredEquivalent.<Item>equivalentScore(equiv, Score.ONE)));

        checking(new Expectations(){{
            one(contentResolver);
                will(returnValue(ResolvedContent.builder().put(ep.getCanonicalUri(), ep).build()));
            one(itemUpdater).updateEquivalences(ep);
                will(returnValue(equivResult));
            one(resultStore).store(equivResult);will(returnValue(equivResult));
            one(contentResolver);
                will(returnValue(ResolvedContent.builder().put(equivParentUri,equivParent).build()));
        }});
        
        Container container = new Container();
        container.setChildRefs(childRefs);
        
        ScoredEquivalents<Container> scores = generator.generate(container, new DefaultDescription());
        
        assertThat(scores.equivalents(), hasEntry(equivParent, Score.ONE));
    }

    private EquivalenceResult<Item> resultFor(final Episode ep, Map<Publisher, ScoredEquivalent<Item>> strong) {
        return new EquivalenceResult<Item>(ep, ImmutableList.<ScoredEquivalents<Item>> of(), null, strong, null);
    }
}
