package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_CONTAINERS;
import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_ITEMS;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.util.List;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingHandler;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ContentEquivalenceUpdateTask extends ScheduledTask {

    private final ContentLister contentStore;
    private final ContentEquivalenceUpdater<Content> rootUpdater;
    private final AdapterLog log;
    private List<Publisher> publishers = ImmutableList.of();
    private String schedulingKey = "equivalence";
    private final ScheduleTaskProgressStore progressStore;
    private final ImmutableSet<ContentTable> topLevelTables = ImmutableSet.of(TOP_LEVEL_CONTAINERS, TOP_LEVEL_ITEMS);
    
    public ContentEquivalenceUpdateTask(ContentLister contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log, ScheduleTaskProgressStore progressStore) {
        this.contentStore = contentStore;
        this.rootUpdater = rootUpdater;
        this.log = log;
        this.progressStore = progressStore;
    }
    
    @Override
    protected void runTask() {
        PublisherListingProgress currentProgress = progressStore.progressForTask(schedulingKey);
        Publisher currentPublisher = currentProgress.getPublisher();
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Started: %s from %s", schedulingKey, startProgress(currentProgress.getUri()))));
        
        int startPublisherIndex = currentPublisher == null ? 0 : publishers.indexOf(currentPublisher);
        for (final Publisher publisher : publishers.subList(startPublisherIndex, publishers.size())){
            
            progressStore.storeProgress(schedulingKey, new PublisherListingProgress(currentProgress, publisher));
            
            ContentListingCriteria criteria = defaultCriteria().startingAt(currentProgress).forPublisher(publisher);
            boolean finished = contentStore.listContent(topLevelTables, criteria, new ContentListingHandler() {
                @Override
                public boolean handle(Iterable<? extends Content> contents, ContentListingProgress progress) {
                    int processed = 100;
                    for (Content content : contents) {
                        updateEquivalence(content); 
                        reportStatus(String.format("Processed %d / %d %s top-level content. %s", progress.count() - --processed, progress.total(), publisher.name(), content.getCanonicalUri()));
                    }
                    progressStore.storeProgress(schedulingKey, new PublisherListingProgress(progress, publisher));
                    return shouldContinue();
                }
            });
            
            if(finished) {
                currentProgress = new PublisherListingProgress(ContentListingProgress.START, null);
            } else {
                log.record(infoEntry().withSource(getClass()).withDescription("Interrupted: %s", schedulingKey));
                return;
            }
        }
        
        progressStore.storeProgress(schedulingKey, new PublisherListingProgress(ContentListingProgress.START, null));
        log.record(infoEntry().withSource(getClass()).withDescription("Finished: %s", schedulingKey));
    }
    
    public void updateEquivalence(Content content) {
        try {
            rootUpdater.updateEquivalences(content);
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+content.getCanonicalUri()));
        }
    }

    private String startProgress(String uri) {
        return uri == null ? "start" : uri;
    }
    
    public ContentEquivalenceUpdateTask forPublishers(Publisher... publishers) {
        this.publishers = ImmutableList.copyOf(publishers);
        this.schedulingKey = Joiner.on("-").join(Iterables.transform(this.publishers, Publisher.TO_KEY))+"-equivalence";
        return this;
    }

}
