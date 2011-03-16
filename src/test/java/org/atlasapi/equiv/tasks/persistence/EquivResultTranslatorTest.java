package org.atlasapi.equiv.tasks.persistence;

import static com.google.common.collect.Ordering.usingToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.equiv.tasks.SuggestedEquivalents;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.stats.Count;
import com.mongodb.DBObject;

public class EquivResultTranslatorTest extends TestCase {

    public void testEncodingAndDecodingEquivResult() {
        
        ContainerEquivResultTranslator translator = new ContainerEquivResultTranslator();
        
        SuggestedEquivalents<Described> suggestions = new SuggestedEquivalents<Described>(ImmutableMap.<Publisher, List<Count<Described>>>of(
                Publisher.BBC, ImmutableList.of(new Count<Described>(new Described("One"), usingToString(), 10), new Count<Described>(new Described("Two"),usingToString(),1)),
                Publisher.C4, ImmutableList.of(new Count<Described>(new Described("Three"),usingToString(),5))
        ));
        
        EquivResult<Described> result = new EquivResult<Described>(new Described("One"), 10, suggestions, 0.9);
        
        DBObject encoded = translator.toDBObject(result);
        
        EquivResult<String> decoded = translator.fromDBObject(encoded);
        
        assertThat(decoded.described(), is(equalTo("null/One")));
        assertThat(ImmutableMap.copyOf(decoded.strongSuggestions()), is(equalTo(ImmutableMap.of(
                Publisher.BBC, "null/One",
                Publisher.C4, "null/Three"
        ))));
    }
    
    
}
