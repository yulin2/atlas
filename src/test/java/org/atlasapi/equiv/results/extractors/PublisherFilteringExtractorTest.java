package org.atlasapi.equiv.results.extractors;

import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

import junit.framework.TestCase;

public class PublisherFilteringExtractorTest extends TestCase {

    public void testFiltersUnacceptablePublishers() {
        
        PublisherFilteringExtractor<Item> extractor = new PublisherFilteringExtractor<Item>(new TopEquivalenceExtractor<Item>());
        
        ScoredEquivalent<Item> paScore = scoreOneFor(Publisher.PA);
        
        assertEquals(Maybe.nothing(), extractor.extract(itemWithPublisher(Publisher.PA), ImmutableList.of(paScore)));
        assertEquals(Maybe.just(paScore), extractor.extract(itemWithPublisher(Publisher.BBC), ImmutableList.of(paScore)));
        assertEquals(Maybe.just(paScore), extractor.extract(itemWithPublisher(Publisher.C4), ImmutableList.of(paScore)));
        
        ScoredEquivalent<Item> BbcScore = scoreOneFor(Publisher.BBC);
        assertEquals(Maybe.nothing(), extractor.extract(itemWithPublisher(Publisher.C4), ImmutableList.of(BbcScore)));
        assertEquals(Maybe.just(BbcScore), extractor.extract(itemWithPublisher(Publisher.SEESAW), ImmutableList.of(BbcScore)));
        
        ScoredEquivalent<Item> dmScore = scoreOneFor(Publisher.DAILYMOTION);
        assertEquals(Maybe.just(dmScore), extractor.extract(itemWithPublisher(Publisher.C4), ImmutableList.of(dmScore)));
        
    }

    private ScoredEquivalent<Item> scoreOneFor(Publisher pub) {
        return ScoredEquivalent.equivalentScore(itemWithPublisher(pub), Score.valueOf(1.0));
    }

    private Item itemWithPublisher(Publisher pub) {
        return new Item("uri", "curie", pub);
    }
    
}
