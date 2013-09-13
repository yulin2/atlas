package org.atlasapi.application.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationCredentials;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.application.model.SourceReadEntry;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.DateTimeZones;

public class MongoApplicationStoreTest {
    private DatabasedMongo adminMongo;
    private ApplicationStore store;
    
    @Before
    public void setup() {
        adminMongo = MongoTestHelper.anEmptyTestDatabase();
        store = new MongoApplicationStore(adminMongo);
    }
    
    @Test
    public void testEncodesAndDecodesApplication() {
        
        final Id applicationId = Id.valueOf(5000);
        final String slug = "app-5000";
        final String title = "test application";
        final String apiKey = "abc123";
        final DateTime created = new DateTime(DateTimeZones.UTC)
               .withDate(2013, 9, 13)
               .withTime(15, 13, 0, 0);
        final SourceReadEntry testEntry1 = new SourceReadEntry(Publisher.BBC, Publisher.BBC.getDefaultSourceStatus());
        final SourceReadEntry testEntry2 = new SourceReadEntry(Publisher.NETFLIX, Publisher.NETFLIX.getDefaultSourceStatus());
        List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
        List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
        ApplicationSources sources = ApplicationSources.builder()
                .withPrecedence(true)
                .withReads(reads)
                .withWrites(writes)
                .build();
        ApplicationCredentials credentials = ApplicationCredentials.builder()
                .withApiKey(apiKey).build();
        
        Application application = Application.builder()
                .withId(applicationId)
                .withSlug(slug)
                .withTitle(title)
                .withCreated(created)
                .withCredentials(credentials)
                .withSources(sources)
                .build();
        
        store.store(application);
        Optional<Application> retrieved = store.applicationFor(applicationId);
        if (!retrieved.isPresent()) {
            fail("No application");
            return;
        }
        // top level
        assertEquals(applicationId, retrieved.get().getId());
        assertEquals(slug, retrieved.get().getSlug());
        assertEquals(title, retrieved.get().getTitle());
        assertEquals(created, retrieved.get().getCreated());
        // Credentials
        assertEquals(apiKey, retrieved.get().getCredentials().getApiKey());
        // Sources - Reads
        assertTrue(retrieved.get().getSources().isPrecedenceEnabled());
        assertEquals(2, retrieved.get().getSources().getReads().size());
        SourceReadEntry retrievedEntry1 = retrieved.get().getSources().getReads().get(0);
        assertEquals(testEntry1.getPublisher(), retrievedEntry1.getPublisher());
        assertEquals(testEntry1.getSourceStatus(), retrievedEntry1.getSourceStatus());
        SourceReadEntry retrievedEntry2 = retrieved.get().getSources().getReads().get(1);
        assertEquals(testEntry2.getPublisher(), retrievedEntry2.getPublisher());
        assertEquals(testEntry2.getSourceStatus(), retrievedEntry2.getSourceStatus());
        // Sources - Writes
        assertTrue(retrieved.get().getSources().getWrites().contains(Publisher.KANDL_TOPICS));
        assertTrue(retrieved.get().getSources().getWrites().contains(Publisher.DBPEDIA));
        assertFalse(retrieved.get().getSources().getWrites().contains(Publisher.BBC));
    }
    
    @After
    public void tearDown() {
        MongoTestHelper.clearDB();
    }
}
