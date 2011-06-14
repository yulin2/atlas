package org.atlasapi.remotesite.pa;

import java.io.File;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.LookupResolvingContentResolver;
import org.atlasapi.persistence.content.mongo.MongoContentResolver;
import org.atlasapi.persistence.content.mongo.MongoContentWriter;
import org.atlasapi.persistence.content.people.DummyItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.persistence.lookup.BasicLookupResolver;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.TimeMachine;

public class PaBaseProgrammeUpdaterTest extends TestCase {

    private PaProgDataProcessor programmeProcessor;

    private TimeMachine clock = new TimeMachine();
    private AdapterLog log = new SystemOutAdapterLog();
    private ContentResolver resolver;
    private ContentWriters contentWriters = new ContentWriters();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabasedMongo db = MongoTestHelper.anEmptyTestDatabase();
        MongoLookupEntryStore lookupStore = new MongoLookupEntryStore(db);
        resolver = new LookupResolvingContentResolver(new MongoContentResolver(db), new BasicLookupResolver(lookupStore));
        
        contentWriters.add(new MongoContentWriter(db, lookupStore, clock));
        programmeProcessor = new PaProgrammeProcessor(contentWriters, resolver, new DummyItemsPeopleWriter(), log);
    }

    public void testShouldCreateCorrectPaData() throws Exception {
    	fail("update this test");
    	
//        TestFileUpdater updater = new TestFileUpdater(programmeProcessor, log);
//        updater.run();
//        Identified content = null;
//
//        // lazy
//        for (int i = 0; i < 10; i++) {
//            Thread.sleep(500);
//            content = resolver.findByCanonicalUris(ImmutableList.of("http://pressassociation.com/brands/122139")).get("http://pressassociation.com/brands/122139").requireValue();
//            if (content != null)
//                continue;
//        }
//
//        assertNotNull(content);
//        assertTrue(content instanceof Brand);
//        Brand brand = (Brand) content;
//        assertFalse(brand.getContents().isEmpty());
//        assertNotNull(brand.getImage());
//
//        Item item = brand.getContents().get(0);
//        assertTrue(item.getCanonicalUri().contains("episodes"));
//        assertNotNull(item.getImage());
//        assertFalse(item.getVersions().isEmpty());
//        assertEquals(MediaType.VIDEO, item.getMediaType());
//        assertEquals(Specialization.TV, item.getSpecialization());
//
//        assertEquals(17, item.people().size());
//        assertEquals(14, item.actors().size());
//
//        Version version = item.getVersions().iterator().next();
//        assertFalse(version.getBroadcasts().isEmpty());
//
//        Broadcast broadcast = version.getBroadcasts().iterator().next();
//        assertEquals("pa:71118471", broadcast.getId());
//
//        updater.run();
//        Thread.sleep(1000);
//
//        content = resolver.findByCanonicalUris(ImmutableList.of("http://pressassociation.com/brands/122139")).get("http://pressassociation.com/brands/122139").requireValue();
//        assertNotNull(content);
//        assertTrue(content instanceof Brand);
//        brand = (Brand) content;
//        assertFalse(brand.getContents().isEmpty());
//
//        item = brand.getContents().get(0);
//        assertFalse(item.getVersions().isEmpty());
//
//        version = item.getVersions().iterator().next();
//        assertFalse(version.getBroadcasts().isEmpty());
//
//        broadcast = version.getBroadcasts().iterator().next();
//        assertEquals("pa:71118471", broadcast.getId());

// Test people get created
//        for (CrewMember crewMember : item.people()) {
//            content = store.findByCanonicalUri(crewMember.getCanonicalUri());
//            assertTrue(content instanceof Person);
//            assertEquals(crewMember.name(), ((Person) content).name());
//        }
    }

    static class TestFileUpdater extends PaBaseProgrammeUpdater {

        public TestFileUpdater(PaProgDataProcessor processor, AdapterLog log) {
            super(processor, new DefaultPaProgrammeDataStore("/data/pa", null), log, null);
        }

        @Override
        public void run() {
            this.processFiles(ImmutableList.of(new File(Resources.getResource("20110115_tvdata.xml").getFile())));
        }
    }
}
