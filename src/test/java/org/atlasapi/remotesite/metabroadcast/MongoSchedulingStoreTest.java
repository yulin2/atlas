package org.atlasapi.remotesite.metabroadcast;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

public class MongoSchedulingStoreTest {

    private static DatabasedMongo mongo;
    private final SchedulingStore store = new MongoSchedulingStore(mongo);

    @BeforeClass
    public static void setup() {
        mongo = MongoTestHelper.anEmptyTestDatabase();
    }
    
    @Test
    public void testCanStoreAndRetrieveMap() {
        store.storeState("key", ImmutableMap.<String, Object>of(
            "property1", "value",
            "property2", 1234L
        ));
        
        Optional<Map<String, Object>> retrievedState = store.retrieveState("key");
        
        assertThat((String)retrievedState.get().get("property1"), is("value"));
        assertThat((Long)retrievedState.get().get("property2"), is(1234L));
    }

}
