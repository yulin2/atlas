package org.atlasapi.equiv.update.tasks;

import java.util.Iterator;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

import com.google.common.annotations.Beta;
import com.metabroadcast.common.scheduling.ScheduledTask;

@Beta
public abstract class AbtractContentListingTask extends ScheduledTask {

    private final ContentLister contentLister;

    public AbtractContentListingTask(ContentLister contentLister) {
        this.contentLister = contentLister;
    }
    
    protected Iterator<Content> getContentIterator(ContentListingCriteria criteria) {
        return contentLister.listContent(criteria);
    }
    
    @Override
    protected void runTask() {

        Iterator<Content> contents = getContentIterator(listingCriteria(ContentListingProgress.START));
        
        int processed = 0;
        
        while(shouldContinue() && contents.hasNext()) {
            Content content = contents.next();
            handle(content);
            reportStatus(String.format("Processed %d. Processing %s", ++processed, content.getCanonicalUri()));
        }
        
    }

    protected abstract void handle(Content content);

    protected abstract ContentListingCriteria listingCriteria(ContentListingProgress progress);
    
}
