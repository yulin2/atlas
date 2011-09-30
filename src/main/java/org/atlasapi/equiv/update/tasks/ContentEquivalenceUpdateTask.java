
package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.persistence.content.ContentCategory.CONTAINER;
import static org.atlasapi.persistence.content.ContentCategory.TOP_LEVEL_ITEM;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFrom;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ContentEquivalenceUpdateTask extends ScheduledTask {

    private final ContentLister contentStore;
    private final ContentEquivalenceUpdater<Content> rootUpdater;
    private final AdapterLog log;
    private List<Publisher> publishers = ImmutableList.of();
    private String schedulingKey = "equivalence";
    private final ScheduleTaskProgressStore progressStore;
    
    public ContentEquivalenceUpdateTask(ContentLister contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log, ScheduleTaskProgressStore progressStore) {
        this.contentStore = contentStore;
        this.rootUpdater = rootUpdater;
        this.log = log;
        this.progressStore = progressStore;
    }
    
    @Override
    protected void runTask() {
        ContentListingProgress currentProgress = progressStore.progressForTask(schedulingKey);
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Started: %s from %s", schedulingKey, startProgress(currentProgress)));
        
        ContentListingCriteria criteria = defaultCriteria().forPublishers(publishers).forContent(CONTAINER, TOP_LEVEL_ITEM).startingAt(currentProgress).build();
        Iterator<Content> contents = contentStore.listContent(criteria);

        int processed = 0;
        boolean shouldContinue = shouldContinue();
        Content content = null;
        
        while (shouldContinue && contents.hasNext()) {
            content = contents.next();
            reportStatus(String.format("Processed %d. Processing %s", processed, content.getCanonicalUri()));
            updateEquivalence(content);
            if(++processed % 100 == 0) {
                progressStore.storeProgress(schedulingKey, progressFrom(content));
            }
            shouldContinue = shouldContinue();
        }
        
        if(shouldContinue) {
            progressStore.storeProgress(schedulingKey, ContentListingProgress.START);
            reportStatus(String.format("Processed %d", processed));
            log.record(infoEntry().withSource(getClass()).withDescription("Finished: %s", schedulingKey));
        } else {
            if(content != null) {
                progressStore.storeProgress(schedulingKey, progressFrom(content));
                log.record(infoEntry().withSource(getClass()).withDescription("Stopped: %s at %s", schedulingKey, content.getCanonicalUri()));
                reportStatus(String.format("Stopped. Processed %d", processed));
            }
        }
    }
    
    public void updateEquivalence(Content content) {
        try {
            rootUpdater.updateEquivalences(content);
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+content.getCanonicalUri()));
        }
    }

    private String startProgress(ContentListingProgress progress) {
        return progress == null ? "start" : String.format("%s %s %s", progress.getCategory(), progress.getPublisher(), progress.getUri());
    }
    
    public ContentEquivalenceUpdateTask forPublishers(Publisher... publishers) {
        this.publishers = ImmutableList.copyOf(publishers);
        this.schedulingKey = Joiner.on("-").join(Iterables.transform(this.publishers, Publisher.TO_KEY))+"-equivalence";
        return this;
    }

}
