package org.atlasapi.messaging.workers;

import org.atlasapi.persistence.messaging.event.EntityUpdatedEvent;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoRecentChangesStore implements RecentChangeStore {

    private DBCollection changeCollection;
    
    private final Function<DBObject, EntityUpdatedEvent> FROM_DBOBJECT = 
        new Function<DBObject, EntityUpdatedEvent>() {
            @Override
            public EntityUpdatedEvent apply(DBObject input) {
                String changeId = TranslatorUtils.toString(input, "cid");
                String entityId = TranslatorUtils.toString(input, "eid");
                String entityType = TranslatorUtils.toString(input, "etype");
                String entitySource = TranslatorUtils.toString(input, "esource");
                return new EntityUpdatedEvent(changeId, entityId, entityType, entitySource);
            }
        };

    public MongoRecentChangesStore(DatabasedMongo mongo) {
        changeCollection = mongo.createCollection("changes", 
            BasicDBObjectBuilder
                .start("capped", true)
                .add("max", 1000)
                .add("size", 100000)
                .get());
    }   

    @Override
    public void logChange(EntityUpdatedEvent event) {
        changeCollection.save(toDBObject(event));
    }

    private DBObject toDBObject(EntityUpdatedEvent event) {
        BasicDBObject dbo = new BasicDBObject();
        dbo.put("cid", event.getChangeId());
        dbo.put("eid", event.getEntityId());
        dbo.put("etype", event.getEntityType());
        dbo.put("esource", event.getEntitySource());
        return dbo;
    }

    @Override
    public Iterable<EntityUpdatedEvent> changes() {
        DBCursor changes = changeCollection.find().sort(new BasicDBObject(MongoConstants.NATURAL, -1));
        return Iterables.transform(changes, FROM_DBOBJECT);
    }

}
