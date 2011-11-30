package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFrom;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.util.Iterator;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;

public abstract class AbstractContentEquivalenceUpdateTask<T extends Content> extends ScheduledTask {

    private final ScheduleTaskProgressStore progressStore;
    private final AdapterLog log;
    
    public AbstractContentEquivalenceUpdateTask(AdapterLog log, ScheduleTaskProgressStore progressStore) {
        this.log = log;
        this.progressStore = progressStore;
    }

    protected abstract Iterator<T> getContentIterator(ContentListingProgress progress);
    
    protected abstract String schedulingKey();

    @Override
    protected void runTask() {
    
        ContentListingProgress currentProgress = progressStore.progressForTask(schedulingKey());
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Started: %s from %s", schedulingKey(), startProgress(currentProgress)));
    
        Iterator<T> contents = getContentIterator(currentProgress);
    
        int processed = 0;
        boolean shouldContinue = shouldContinue();
        T content = null;
    
        try {
            while (shouldContinue && contents.hasNext()) {
                try {
                    content = contents.next();
                    reportStatus(String.format("Processed %d. Processing %s", processed, content.getCanonicalUri()));
    
                    handle(content);
    
                    if (++processed % 100 == 0) {
                        updateProgress(progressFrom(content));
                    }
                    shouldContinue = shouldContinue();
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for " + content.getCanonicalUri()));
                }
            }
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception running task " + schedulingKey()));
            persistProgress(false, content);
            throw Throwables.propagate(e);
        }
        reportStatus(String.format("Processed %d", processed));
        persistProgress(shouldContinue, content);
    }

    protected abstract void handle(T content);

    private void persistProgress(boolean finished, Content content) {
        if (finished) {
            updateProgress(ContentListingProgress.START);
            log.record(infoEntry().withSource(getClass()).withDescription("Finished: %s", schedulingKey()));
        } else {
            if (content != null) {
                updateProgress(progressFrom(content));
                log.record(infoEntry().withSource(getClass()).withDescription("Stopped: %s at %s", schedulingKey(), content.getCanonicalUri()));
            }
        }
    }

    private void updateProgress(ContentListingProgress progress) {
        progressStore.storeProgress(schedulingKey(), progress);
    }

    private String startProgress(ContentListingProgress progress) {
        if (progress == null || ContentListingProgress.START.equals(progress)) {
            return "start";
        }
        return String.format("%s %s %s", progress.getCategory(), progress.getPublisher(), progress.getUri());
    }

}