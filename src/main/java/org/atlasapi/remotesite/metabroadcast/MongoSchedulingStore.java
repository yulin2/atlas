package org.atlasapi.remotesite.metabroadcast;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.UPSERT;

import java.util.Map;

import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoSchedulingStore implements SchedulingStore {

    private final DBCollection collection;

    public MongoSchedulingStore(DatabasedMongo mongo) {
        collection = mongo.collection("scheduling");
    }

    @Override
    public void storeState(String key, Map<String, Object> value) {
        DBObject dbo = new BasicDBObject(value);
        dbo.put(ID, key);
        collection.update(new BasicDBObject(ID, key), dbo, UPSERT, SINGLE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> retrieveState(String key) {
        DBObject storedState = collection.findOne(key);
        if (storedState != null) {
            return Optional.of((Map<String, Object>) storedState.toMap());
        }
        return Optional.absent();
    }

}
