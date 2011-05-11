package org.atlasapi.equiv.update;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ContentEquivalenceUpdateTask extends ScheduledTask {

    private static final int BATCH_SIZE = 10;
    
    private final MongoDbBackedContentStore contentStore;
    private final AdapterLog log;
    private final ContentEquivalenceUpdater<Content> rootUpdater;
    
    private final AtomicInteger processed = new AtomicInteger();

    private Predicate<Content> publisherFilter = Predicates.alwaysTrue();

    
    public ContentEquivalenceUpdateTask(MongoDbBackedContentStore contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log) {
        this.contentStore = contentStore;
        this.rootUpdater = rootUpdater;
        this.log = log;
    }
    
    public ContentEquivalenceUpdateTask withSourcePublishers(final Iterable<Publisher> sourcePublishers) {
        this.publisherFilter = new Predicate<Content>() {
            @Override
            public boolean apply(Content input) {
                return ImmutableSet.copyOf(sourcePublishers).contains(input.getPublisher());
            }
        };
        return this;
    }
    
    @Override
    protected void runTask() {
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Start equivalence task"));
        
        processed.set(0);
        
        String lastId = getLastId();
        List<Content> contents;
        do {
            contents = contentStore.listAllRoots(lastId, -BATCH_SIZE);
            for (Content content : filter(contents)) {
                try {
                    rootUpdater.updateEquivalences(content);
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+content.getCanonicalUri()));
                }
                reportStatus(String.format("Processed %d", processed.incrementAndGet()));
            }
            
            lastId = updateLastId(contents);
        } while (!contents.isEmpty() && shouldContinue());
        
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Finish equivalence task. %s brands processed", processed)));
    }

    private Iterable<Content> filter(List<Content> contents) {
        Predicate<Content> filter = Predicates.and(Predicates.instanceOf(Brand.class), publisherFilter);
        return Iterables.filter(contents, filter);
    }

    private String getLastId() {
        //TODO read persited value from db
        return null;
    }

    private String updateLastId(List<Content> contents) {
        String lastId = !contents.isEmpty() ? Iterables.getLast(contents).getCanonicalUri() : null;
        //TODO persist last ID to db
        return lastId;
    }

}
