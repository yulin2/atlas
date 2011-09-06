package org.atlasapi.equiv.update.tasks;

public interface ScheduleTaskProgressStore {

    PublisherListingProgress progressForTask(String taskName);
    
    void storeProgress(String taskName, PublisherListingProgress progress);
    
}
