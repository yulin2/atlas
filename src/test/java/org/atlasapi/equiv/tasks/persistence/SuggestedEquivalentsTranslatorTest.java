package org.atlasapi.equiv.tasks.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.equiv.tasks.SuggestedEquivalents;
import org.atlasapi.equiv.tasks.persistence.SuggestedEquivalentsTranslator;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.stats.Count;
import com.mongodb.DBObject;

public class SuggestedEquivalentsTranslatorTest extends TestCase {

    public void testEncodingAndDecoding() {
        
        SuggestedEquivalentsTranslator translator = new SuggestedEquivalentsTranslator();
        
        SuggestedEquivalents<String> suggestions = new SuggestedEquivalents<String>(ImmutableMap.<Publisher, List<Count<String>>>of(
                Publisher.BBC, ImmutableList.of(new Count<String>("One",10), new Count<String>("Two",1)),
                Publisher.C4, ImmutableList.of(new Count<String>("Three",5))
        ));
        
        DBObject encoded = translator.toDBObject(suggestions);
        
        SuggestedEquivalents<String> decoded = translator.fromDBObject(encoded);
        
        assertThat(suggestions, is(equalTo(decoded)));
        
    }
    
}
