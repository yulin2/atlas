package org.atlasapi.equiv.update.tasks;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoScheduleTaskProgressStore implements ScheduleTaskProgressStore {
    
    private DBCollection collection;

    public MongoScheduleTaskProgressStore(DatabasedMongo mongo) {
        collection = mongo.collection("scheduling");
    }

    @Override
    public PublisherListingProgress progressForTask(String taskName) {
        DBObject existing = collection.findOne(taskName);
        return existing != null ? fromDbo(existing) : new PublisherListingProgress(ContentListingProgress.START, null);
    }

    private PublisherListingProgress fromDbo(DBObject progress) {
        String lastId = TranslatorUtils.toString(progress, "lastId");
        String tableName = TranslatorUtils.toString(progress, "collection");
        ContentTable table = tableName == null ? null : ContentTable.fromString(tableName);
        
        String pubKey = TranslatorUtils.toString(progress, "publisher");
        Publisher publisher = pubKey == null ? null : Publisher.fromKey(pubKey).valueOrNull();
        
        return (PublisherListingProgress) new PublisherListingProgress(lastId, table, publisher)
                .withCount(TranslatorUtils.toInteger(progress, "count"))
                .withTotal(TranslatorUtils.toInteger(progress, "total"));
    }

    @Override
    public void storeProgress(String taskName, PublisherListingProgress progress) {
        DBObject update = new BasicDBObject();
        TranslatorUtils.from(update, "lastId", progress.getUri() == null ? "start" : progress.getUri());
        TranslatorUtils.from(update, "collection",  progress.getTable() == null ? null : progress.getTable().toString());
        TranslatorUtils.from(update, "total", progress.total());
        TranslatorUtils.from(update, "count", progress.count());
        TranslatorUtils.from(update, "publisher", progress.getPublisher() == null ? null : progress.getPublisher().key());
        
        collection.update(where().fieldEquals(ID, taskName).build(), new BasicDBObject(MongoConstants.SET, update), true, false);
    }

}
