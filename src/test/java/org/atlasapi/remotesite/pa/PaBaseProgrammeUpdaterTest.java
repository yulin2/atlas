package org.atlasapi.remotesite.pa;

import java.io.File;

import junit.framework.TestCase;

import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.remotesite.ContentWriters;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.time.TimeMachine;

public class PaBaseProgrammeUpdaterTest extends TestCase {
    
    private PaProgrammeProcessor programmeProcessor;

    private TimeMachine clock = new TimeMachine();
    private AdapterLog log = new SystemOutAdapterLog();
    private MongoDbBackedContentStore store;
    private ContentWriters contentWriters = new ContentWriters();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = new MongoDbBackedContentStore(MongoTestHelper.anEmptyTestDatabase(), clock);
        contentWriters.add(store);
        programmeProcessor = new PaProgrammeProcessor(contentWriters, store, log);
    }
    
    public void testShouldCreateCorrectPaData() throws Exception {
        TestFileUpdater updater = new TestFileUpdater(programmeProcessor, log);
        updater.run();
        
        assertNotNull(store.findByUri("http://pressassociation.com/brands/122139"));
    }
    
    static class TestFileUpdater extends PaBaseProgrammeUpdater {

        public TestFileUpdater(PaProgrammeProcessor processor, AdapterLog log) {
            super(processor, log);
        }

        @Override
        public void run() {
            this.processFiles(ImmutableList.of(new File(Resources.getResource("20110115_tvdata.xml").getFile())));
        }
    }
}
