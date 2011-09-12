package org.atlasapi.equiv.update.tasks;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

import com.metabroadcast.common.persistence.MongoTestHelper;

import junit.framework.TestCase;

public class MongoScheduleTaskProgressStoreTest extends TestCase {

    private String taskName = "iamatask";
    private final MongoScheduleTaskProgressStore store = new MongoScheduleTaskProgressStore(MongoTestHelper.anEmptyTestDatabase());

    public void testSavingProgress() {

        PublisherListingProgress progress = new PublisherListingProgress(new ContentListingProgress("one", ContentTable.CHILD_ITEMS), Publisher.BBC);
        progress.withCount(5).withTotal(6);
        
        store.storeProgress(taskName, progress);
        
        PublisherListingProgress restored = store.progressForTask(taskName);
        
        assertEquals(progress.getUri(), restored.getUri());
        assertEquals(progress.getTable(), restored.getTable());
        assertEquals(progress.count(), restored.count());
        assertEquals(progress.total(), restored.total());
        assertEquals(progress.getPublisher(), restored.getPublisher());
        
        progress = new PublisherListingProgress(new ContentListingProgress(null, null), null);
        
        store.storeProgress(taskName, progress);
        
        restored = store.progressForTask(taskName);
        
        assertEquals("start", restored.getUri());
        assertEquals(progress.getTable(), restored.getTable());
        assertEquals(progress.count(), restored.count());
        assertEquals(progress.total(), restored.total());
        assertEquals(progress.getPublisher(), restored.getPublisher());
        
    }
    
    
}
