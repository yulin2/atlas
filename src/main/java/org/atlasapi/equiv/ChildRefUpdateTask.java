package org.atlasapi.equiv;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static org.atlasapi.persistence.content.ContentCategory.CONTAINERS;
import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFrom;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.equiv.update.tasks.ScheduleTaskProgressStore;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentCategory;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.content.mongo.MongoContentTables;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.media.entity.ContainerTranslator;
import org.atlasapi.persistence.media.entity.IdentifiedTranslator;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class ChildRefUpdateTask extends ScheduledTask {

    private final ContentLister contentStore;
    private final ContentResolver resolver;
    private final ScheduleTaskProgressStore progressStore;

    private final DBCollection containers;
    private final DBCollection programmeGroups;
    private final ContainerTranslator translator = new ContainerTranslator();

    private final AdapterLog log;

    private ImmutableList<Publisher> publishers;
    private String scheduleKey;

    public ChildRefUpdateTask(ContentLister lister, ContentResolver resolver, DatabasedMongo mongo, ScheduleTaskProgressStore progressStore, AdapterLog log) {
        this.contentStore = lister;
        this.resolver = resolver;
        
        MongoContentTables contentTables = new MongoContentTables(mongo);
        containers = contentTables.collectionFor(ContentCategory.CONTAINER);
        programmeGroups = contentTables.collectionFor(ContentCategory.PROGRAMME_GROUP);
        
        this.log = log;
        
        this.progressStore = progressStore;
    }
    
    public ChildRefUpdateTask forPublishers(Publisher... publishers) {
        this.publishers = ImmutableList.copyOf(publishers);
        this.scheduleKey = "childref" + Joiner.on("-").join(this.publishers);
        return this;
    }

    @Override
    protected void runTask() {
        
        ContentListingProgress progress = progressStore.progressForTask(scheduleKey);
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Started: %s from %s", scheduleKey, startProgress(progress)));
        
        Iterator<Content> children = contentStore.listContent(defaultCriteria().forPublishers(publishers).forContent(ImmutableList.copyOf(CONTAINERS)).startingAt(progress).build());

        int processed = 0;
        boolean shouldContinue = shouldContinue();
        Content content = null;

        try {
            while (children.hasNext() && shouldContinue) {
                try {
                    content = children.next();
                    updateChildRefs((Container) content);
                    reportStatus(String.format("Processed %d. Processing %s", processed, content.getCanonicalUri()));
                    if (++processed % 100 == 0) {
                        updateProgress(progressFrom(content));
                    }
                    shouldContinue = shouldContinue();
                } catch (Exception e) {
                    log.record(warnEntry().withCause(e).withDescription("Child Ref update failed for " + content.getCanonicalUri()).withSource(getClass()));
                }
            }
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception running task " + scheduleKey));
            persistProgress(false, content);
            throw Throwables.propagate(e);
        }
        reportStatus(String.format("Processed %d", processed));
        persistProgress(shouldContinue, content);
    }

    public void updateChildRefs(Container container) {
        ResolvedContent children = resolver.findByCanonicalUris(Iterables.transform(container.getChildRefs(), ChildRef.TO_URI));
        List<ChildRef> refs = Lists.newArrayList();
        for (Identified child : children.getAllResolvedResults()) {
            refs.add(((Item)child).childRef());
        }
        container.setChildRefs(ChildRef.dedupeAndSort(refs));
        write(container);
    }

    private void write(Container container) {
        DBObject containerDbo = translator.toDBO(container, true);
        if (container instanceof Series) {
            createOrUpdateContainer(container, programmeGroups, containerDbo);
            if(((Series) container).getParent() != null) {
                return;
            }
        }
        createOrUpdateContainer(container, containers, containerDbo);
    }
    
    private void createOrUpdateContainer(Container container, DBCollection collection, DBObject containerDbo) {
        MongoQueryBuilder where = where().fieldEquals(IdentifiedTranslator.CANONICAL_URI, container.getCanonicalUri());
        collection.update(where.build(), set(containerDbo), true, false);
    }

    private BasicDBObject set(DBObject dbo) {
        dbo.removeField(MongoConstants.ID);
        BasicDBObject containerUpdate = new BasicDBObject(MongoConstants.SET, dbo);
        return containerUpdate;
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
