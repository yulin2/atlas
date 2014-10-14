package org.atlasapi;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.mongodb.DBObject;
import com.mongodb.TaggableReadPreference;


public class MongoSecondaryReadPreferenceBuilderTest {

    private final MongoSecondaryReadPreferenceBuilder builder = new MongoSecondaryReadPreferenceBuilder();
    
    @Test
    public void testNoTags() {
        TaggableReadPreference readPreference = (TaggableReadPreference) builder.fromProperties(ImmutableSet.<String>of());
        
        assertTrue(readPreference.isSlaveOk());
        assertTrue(readPreference.getTagSets().isEmpty());
    }
    
    @Test
    public void testWithSingleTag() {
        TaggableReadPreference readPreference = (TaggableReadPreference) builder.fromProperties(ImmutableSet.<String>of("key:value"));
        DBObject dbo = Iterables.getOnlyElement(readPreference.getTagSets());
        assertTrue(readPreference.isSlaveOk());
        assertThat((String)dbo.get("key"), is(equalTo("value")));
    }
    
    @Test
    public void testWithMultipleTags() {
        TaggableReadPreference readPreference = (TaggableReadPreference) 
                builder.fromProperties(ImmutableSet.<String>of("key:value", "key2:value2"));
        
        List<DBObject> dbos = readPreference.getTagSets();
        assertTrue(readPreference.isSlaveOk());
        
        List<DBObject> dbos = readPreference.getTagSets();        
        assertThat((String)dbos.get(0).get("key"), is(equalTo("value")));
        assertThat((String)dbos.get(1).get("key2"), is(equalTo("value2")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidTags() {
        builder.fromProperties(ImmutableSet.<String>of("key:value:other"));
    }
}
