package org.atlasapi.equiv.results.extractors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.filters.PublisherFilter;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

public class PublisherFilteringExtractorTest {

    @Test
    public void testFiltersUnacceptablePublishers() {
        
        PublisherFilter<Item> filter = new PublisherFilter<Item>();
        
        ScoredCandidate<Item> paScore = scoreOneFor(Publisher.PA);
        
        assertFalse(filter.apply(paScore, itemWithPublisher(Publisher.PA), new DefaultDescription()));
        assertTrue(filter.apply(paScore, itemWithPublisher(Publisher.BBC), new DefaultDescription()));
        assertTrue(filter.apply(paScore, itemWithPublisher(Publisher.C4), new DefaultDescription()));
        
        ScoredCandidate<Item> BbcScore = scoreOneFor(Publisher.BBC);
        assertFalse(filter.apply(BbcScore, itemWithPublisher(Publisher.C4), new DefaultDescription()));
        assertTrue(filter.apply(BbcScore, itemWithPublisher(Publisher.SEESAW), new DefaultDescription()));
        
        ScoredCandidate<Item> dmScore = scoreOneFor(Publisher.DAILYMOTION);
        assertTrue(filter.apply(dmScore, itemWithPublisher(Publisher.C4), new DefaultDescription()));
        
    }

    private ScoredCandidate<Item> scoreOneFor(Publisher pub) {
        return ScoredCandidate.valueOf(itemWithPublisher(pub), Score.valueOf(1.0));
    }

    private Item itemWithPublisher(Publisher pub) {
        return new Item("uri", "curie", pub);
    }
    
}
