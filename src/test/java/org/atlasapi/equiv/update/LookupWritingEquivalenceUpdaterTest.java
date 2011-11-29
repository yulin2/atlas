package org.atlasapi.equiv.update;

import static org.hamcrest.Matchers.hasItems;

import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.lookup.LookupWriter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.Duration;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class LookupWritingEquivalenceUpdaterTest extends MockObjectTestCase {

    @SuppressWarnings("unchecked")
    private final ContentEquivalenceUpdater<Item> delegate = mock(ContentEquivalenceUpdater.class);
    private final LookupWriter lookupWriter = mock(LookupWriter.class);
    
    public void testWritesLookups() {
        
        LookupWritingEquivalenceUpdater<Item> updater = new LookupWritingEquivalenceUpdater<Item>(delegate, lookupWriter);
        
        final Item content = new Item("item","c:item", Publisher.BBC);
        final Item equiv1 = new Item("equiv1","c:equiv1",Publisher.PA);
        final Item equiv2 = new Item("equiv2","c:equiv2",Publisher.ITV);
        
        final EquivalenceResult<Item> equivResult = equivResultFor(content, ImmutableList.of(equiv1,equiv2));
        
        checking(new Expectations(){{
            one(delegate).updateEquivalences(content);will(returnValue(equivResult));
            one(lookupWriter).writeLookup(with(content), with(hasItems(equiv1,equiv2)));
        }});
        
        updater.updateEquivalences(content);
        
    }
    
    @SuppressWarnings("unchecked")
    public void testDoesntWriteLookupsForItemWhichWasSeenAsEquivalentButDoesntAssertAnyEquivalences() {
        
        LookupWritingEquivalenceUpdater<Item> updater = new LookupWritingEquivalenceUpdater<Item>(delegate, lookupWriter);
        
        final Item content = new Item("item","c:item", Publisher.BBC);
        final Item equiv1 = new Item("equiv1","c:equiv1",Publisher.PA);
        final Item equiv2 = new Item("equiv2","c:equiv2",Publisher.ITV);
        
        final EquivalenceResult<Item> equivResult = equivResultFor(content, ImmutableList.of(equiv1,equiv2));
        final EquivalenceResult<Item> noEquivalences = equivResultFor(equiv1, ImmutableList.<Item>of());
        
        checking(new Expectations(){{
            one(delegate).updateEquivalences(content);will(returnValue(equivResult));
            one(lookupWriter).writeLookup(with(content), with(hasItems(equiv1,equiv2)));
            one(delegate).updateEquivalences(equiv1);will(returnValue(noEquivalences));
            never(lookupWriter).writeLookup(with(equiv1), with(any(Iterable.class)));
        }});
        
        updater.updateEquivalences(content);
        updater.updateEquivalences(equiv1);
        
    }
    
    public void testWritesLookupsForItemWhichWasSeenAsEquivalentButDoesntAssertAnyEquivalencesWhenCacheTimesOut() throws InterruptedException {
        
        Duration cacheDuration = new Duration(5);
        LookupWritingEquivalenceUpdater<Item> updater = new LookupWritingEquivalenceUpdater<Item>(delegate, lookupWriter, cacheDuration);
        
        final Item content = new Item("item","c:item", Publisher.BBC);
        final Item equiv1 = new Item("equiv1","c:equiv1",Publisher.PA);
        final Item equiv2 = new Item("equiv2","c:equiv2",Publisher.ITV);
        
        final EquivalenceResult<Item> equivResult1 = equivResultFor(content, ImmutableList.of(equiv1,equiv2));
        final EquivalenceResult<Item> equivResult2 = equivResultFor(equiv1, ImmutableList.<Item>of(content));
        
        checking(new Expectations(){{
            one(delegate).updateEquivalences(content);will(returnValue(equivResult1));
            one(lookupWriter).writeLookup(with(content), with(hasItems(equiv1,equiv2)));
            one(delegate).updateEquivalences(equiv1);will(returnValue(equivResult2));
            one(lookupWriter).writeLookup(with(equiv1), with(hasItems(content)));
        }});
        
        updater.updateEquivalences(content);
        Thread.sleep(cacheDuration.getMillis()*2);
        updater.updateEquivalences(equiv1);
        
    }
    
    private EquivalenceResult<Item> equivResultFor(Item content, Iterable<Item> equivalents) {
        Map<Publisher, ScoredEquivalent<Item>> strong = Maps.transformValues(Maps.uniqueIndex(equivalents, TO_PUBLISHER),RANDOM_SCORE);
        return new EquivalenceResult<Item>(content, ImmutableList.<ScoredEquivalents<Item>>of(), null, strong , null);
    }
    
    private static final Function<Content, Publisher> TO_PUBLISHER = new Function<Content, Publisher>() {

        @Override
        public Publisher apply(Content input) {
            return input.getPublisher();
        }
    };

    private static final Function<Item, ScoredEquivalent<Item>> RANDOM_SCORE = new Function<Item, ScoredEquivalent<Item>>() {
        @Override
        public ScoredEquivalent<Item> apply(Item input) {
            //Chosen by fair dice roll / 10. Guaranteed to be random.
            return ScoredEquivalent.equivalentScore(input, Score.valueOf(0.4));
        }
    };
}
