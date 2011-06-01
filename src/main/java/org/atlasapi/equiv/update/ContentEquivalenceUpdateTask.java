package org.atlasapi.equiv.update;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.RetrospectiveContentLister;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class ContentEquivalenceUpdateTask extends ScheduledTask {

    private static final int BATCH_SIZE = 10;
    
    private final RetrospectiveContentLister contentStore;
    private final ContentEquivalenceUpdater<Content> rootUpdater;
    private final AdapterLog log;
    private final DBCollection scheduling;
    
    private final AtomicInteger processed = new AtomicInteger();

    private Predicate<Content> publisherFilter = Predicates.alwaysTrue();



    
    public ContentEquivalenceUpdateTask(RetrospectiveContentLister contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log, DatabasedMongo db) {
        this.contentStore = contentStore;
        this.rootUpdater = rootUpdater;
        this.log = log;
        this.scheduling = db.collection("scheduling");
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
        String lastId = getLastId();
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Start equivalence task from %s", lastId)));
        
        processed.set(0);
        
        List<Content> contents;
        do {
            contents = contentStore.listAllRoots(lastId, -BATCH_SIZE);
            for (Content content : filter(contents)) {
                try {
                    /*EquivalenceResult<Content> result = */rootUpdater.updateEquivalences(content);
                    //TODO filter to avoid repetition
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+content.getCanonicalUri()));
                }
                reportStatus(String.format("Processed %d top-level content", processed.incrementAndGet()));
            }
            
            lastId = updateLastId(contents);
        } while (!contents.isEmpty() && shouldContinue());
        
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Finish equivalence task. %s brands processed", processed)));
    }

    private Iterable<Content> filter(List<Content> contents) {
        Predicate<Content> filter = /*Predicates.and(Predicates.instanceOf(Brand.class), */publisherFilter/*)*/;
        return Iterables.filter(contents, filter);
    }

    private String getLastId() {
        DBObject lastId = scheduling.findOne("equivalence");
        return lastId != null ? TranslatorUtils.toString(lastId, "lastId") : null;
    }

    private String updateLastId(List<Content> contents) {
        String lastId = !contents.isEmpty() ? Iterables.getLast(contents).getCanonicalUri() : null;
        scheduling.update(where().fieldEquals(ID, "equivalence").build(), update().setField("lastId", lastId).build(), true, false);
        return lastId;
    }

}
