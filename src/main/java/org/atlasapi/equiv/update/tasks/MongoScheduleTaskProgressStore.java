package org.atlasapi.equiv.update.tasks;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.UPSERT;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoUpdateBuilder;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoScheduleTaskProgressStore implements ScheduleTaskProgressStore {
    
    private static final String SCHEDULING_COLLECTION_NAME = "scheduling";
    private static final String COUNT = "count";
    private static final String TOTAL = "total";
    private static final String START = "start";
    private static final String PUBLISHER = "publisher";
    private static final String LAST_ID = "lastId";
    private static final String COLLECTION = "collection";
    
    private final DBCollection collection;

    public MongoScheduleTaskProgressStore(DatabasedMongo mongo) {
        collection = mongo.collection(SCHEDULING_COLLECTION_NAME);
    }

    @Override
    public PublisherListingProgress progressForTask(String taskName) {
        DBObject existing = collection.findOne(taskName);
        return existing != null ? fromDbo(existing) : new PublisherListingProgress(ContentListingProgress.START, null);
    }

    private PublisherListingProgress fromDbo(DBObject progress) {
        String lastId = TranslatorUtils.toString(progress, LAST_ID);
        String tableName = TranslatorUtils.toString(progress, COLLECTION);
        ContentTable table = tableName == null ? null : ContentTable.fromString(tableName);
        
        String pubKey = TranslatorUtils.toString(progress, PUBLISHER);
        Publisher publisher = pubKey == null ? null : Publisher.fromKey(pubKey).valueOrNull();
        
        return (PublisherListingProgress) new PublisherListingProgress(lastId, table, publisher)
                .withCount(TranslatorUtils.toInteger(progress, COUNT))
                .withTotal(TranslatorUtils.toInteger(progress, TOTAL));
    }

    @Override
    public void storeProgress(String taskName, PublisherListingProgress progress) {
        MongoUpdateBuilder update = update()
            .setField(LAST_ID, progress.getUri() == null ? START : progress.getUri())
            .setField(TOTAL, progress.total())
            .setField(COUNT, progress.count());
        
        if(progress.getTable() != null) {
            update.setField(COLLECTION, progress.getTable().toString());
        } else {
            update.unsetField(COLLECTION);
        }
        
        if(progress.getPublisher() != null) {
            update.setField(PUBLISHER, progress.getPublisher().key());
        } else {
            update.unsetField(PUBLISHER);
        }
        
        collection.update(where().fieldEquals(ID, taskName).build(), update.build(), UPSERT, SINGLE);
    }

}
