package org.atlasapi.equiv.update.tasks;

import org.atlasapi.persistence.content.listing.ContentListingProgress;

public interface ScheduleTaskProgressStore {

    ContentListingProgress progressForTask(String taskName);
    
    void storeProgress(String taskName, ContentListingProgress progress);
    
}
