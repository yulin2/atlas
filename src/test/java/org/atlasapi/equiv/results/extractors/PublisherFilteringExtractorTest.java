package org.atlasapi.equiv.results.extractors;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.Publisher;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

import junit.framework.TestCase;

public class PublisherFilteringExtractorTest extends TestCase {

    public void testFiltersUnacceptablePublishers() {
        
        PublisherFilteringExtractor<Item> extractor = new PublisherFilteringExtractor<Item>(new TopEquivalenceExtractor<Item>());
        
        ScoredEquivalent<Item> paScore = scoreOneFor(Publisher.PA);
        
        assertEquals(Maybe.nothing(), extractor.extract(itemWithPublisher(Publisher.PA), ImmutableList.of(paScore), new DefaultDescription()));
        assertEquals(Maybe.just(paScore), extractor.extract(itemWithPublisher(Publisher.BBC), ImmutableList.of(paScore), new DefaultDescription()));
        assertEquals(Maybe.just(paScore), extractor.extract(itemWithPublisher(Publisher.C4), ImmutableList.of(paScore), new DefaultDescription()));
        
        ScoredEquivalent<Item> BbcScore = scoreOneFor(Publisher.BBC);
        assertEquals(Maybe.nothing(), extractor.extract(itemWithPublisher(Publisher.C4), ImmutableList.of(BbcScore), new DefaultDescription()));
        assertEquals(Maybe.just(BbcScore), extractor.extract(itemWithPublisher(Publisher.SEESAW), ImmutableList.of(BbcScore), new DefaultDescription()));
        
        ScoredEquivalent<Item> dmScore = scoreOneFor(Publisher.DAILYMOTION);
        assertEquals(Maybe.just(dmScore), extractor.extract(itemWithPublisher(Publisher.C4), ImmutableList.of(dmScore), new DefaultDescription()));
        
    }

    private ScoredEquivalent<Item> scoreOneFor(Publisher pub) {
        return ScoredEquivalent.equivalentScore(itemWithPublisher(pub), Score.valueOf(1.0));
    }

    private Item itemWithPublisher(Publisher pub) {
        return new Item("uri", "curie", pub);
    }
    
}
