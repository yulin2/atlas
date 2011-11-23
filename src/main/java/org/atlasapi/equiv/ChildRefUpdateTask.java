package org.atlasapi.equiv;

import static org.atlasapi.persistence.content.ContentCategory.CHILD_ITEM;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFrom;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.util.Iterator;

import org.atlasapi.equiv.update.tasks.ScheduleTaskProgressStore;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.content.mongo.ChildRefWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ChildRefUpdateTask extends ScheduledTask {

    private final ContentLister contentStore;
    private final ChildRefWriter childRefWriter;
    private final ImmutableList<Publisher> publishers;
    private final ScheduleTaskProgressStore progressStore;
    private final String scheduleKey;
    private final AdapterLog log;

    public ChildRefUpdateTask(ContentLister contentLister, ChildRefWriter refWriter, ScheduleTaskProgressStore progressStore, AdapterLog log, Publisher... publishers) {
        this.contentStore = contentLister;
        this.progressStore = progressStore;
        this.log = log;
        this.publishers = ImmutableList.copyOf(publishers);
        this.childRefWriter = refWriter;
        this.scheduleKey = "childref" + Joiner.on("-").join(this.publishers);
    }

    @Override
    protected void runTask() {
        
        ContentListingProgress progress = progressStore.progressForTask(scheduleKey);
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Started: %s from %s", scheduleKey, startProgress(progress)));
        
        Iterator<Content> children = contentStore.listContent(defaultCriteria().forPublishers(publishers).forContent(CHILD_ITEM).startingAt(progress).build());

        int processed = 0;
        boolean shouldContinue = shouldContinue();
        Content content = null;

        try {
            while (children.hasNext() && shouldContinue) {
                content = children.next();
                if (content instanceof Episode) {
                    childRefWriter.includeEpisodeInSeriesAndBrand((Episode) content);
                } else if (content instanceof Item) {
                    childRefWriter.includeItemInTopLevelContainer((Item) content);
                }
                reportStatus(progress.toString());
                if (++processed % 100 == 0) {
                    updateProgress(progressFrom(content));
                }
                shouldContinue = shouldContinue();
            }
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception running task " + scheduleKey));
            persistProgress(false, content);
            throw Throwables.propagate(e);
        }
        reportStatus(String.format("Processed %d", processed));
        persistProgress(shouldContinue, content);
    }

    public void updateProgress(ContentListingProgress progress) {
        progressStore.storeProgress(scheduleKey, progress);
    }
    
    private void persistProgress(boolean finished, Content content) {
        if (finished) {
            updateProgress(ContentListingProgress.START);
            log.record(infoEntry().withSource(getClass()).withDescription("Finished: %s", scheduleKey));
        } else {
            if (content != null) {
                updateProgress(progressFrom(content));
                log.record(infoEntry().withSource(getClass()).withDescription("Stopped: %s at %s", scheduleKey, content.getCanonicalUri()));
            }
        }
    }

    private String startProgress(ContentListingProgress progress) {
        if (progress == null || ContentListingProgress.START.equals(progress)) {
            return "start";
        }
        return String.format("%s %s %s", progress.getCategory(), progress.getPublisher(), progress.getUri());
    }
}
