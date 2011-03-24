package org.atlasapi.equiv.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.stats.Count;

public class SuggestedEquivalentsTest extends TestCase {

    public void testSuggestedEquivalentsCreationFromMulitmap() {
        
        ListMultimap<Publisher, Described> input = ImmutableListMultimap.<Publisher,Described>builder()
                .put(Publisher.BBC, new Described("one"))
                .put(Publisher.BBC, new Described("one"))
                .put(Publisher.BBC, new Described("two"))
                .put(Publisher.BBC, new Described("one"))
                .put(Publisher.BBC, new Described("two"))
                .put(Publisher.C4, new Described("three"))
                .put(Publisher.C4, new Described("three"))
                .put(Publisher.C4, new Described("three")).build();
        
        SuggestedEquivalents<Described> suggested = SuggestedEquivalents.from(input);
        
        Map<Publisher,List<Count<Described>>> processed = suggested.getBinnedCountedSuggestions();
        
        assertThat(ImmutableSet.copyOf(processed.keySet()), is(equalTo(ImmutableSet.of(Publisher.BBC, Publisher.C4))));
        
        assertThat(processed.get(Publisher.BBC).size(), is(2));
        assertThat(processed.get(Publisher.BBC).get(0), is(equalTo(new Count<Described>(new Described("one"), Ordering.usingToString(), 3))));
        assertThat(processed.get(Publisher.BBC).get(1), is(equalTo(new Count<Described>(new Described("two"), Ordering.usingToString(), 2))));

        assertThat(processed.get(Publisher.C4).size(), is(1));
        assertThat(processed.get(Publisher.C4).get(0), is(equalTo(new Count<Described>(new Described("three"), Ordering.usingToString(), 3))));
        
    }
    
    public void testSuggestedEquivalentsCreationWithNullValues() {
        ListMultimap<Publisher, Described> input = ArrayListMultimap.create();
        input.put(Publisher.BBC, null);
        input.put(Publisher.BBC, null);

        SuggestedEquivalents<Described> suggested = SuggestedEquivalents.from(input);
        
        Map<Publisher,List<Count<Described>>> processed = suggested.getBinnedCountedSuggestions();
        
        assertTrue(processed.isEmpty());
    }
    
}
