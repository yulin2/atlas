package org.atlasapi.remotesite.metabroadcast.picks;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


public class MongoPicksLastProcessedStore implements PicksLastProcessedStore {

    private static final String KEY = "lastProcessedScheduleDay";
    private static final String DATE_KEY = "lastProcessed";
    private static final String COLLECTION = "scheduledTaskStatus.metabroadcastPicks";
    private final DBCollection collection;
    
    public MongoPicksLastProcessedStore(DatabasedMongo mongo) {
        collection = mongo.collection(COLLECTION);
    }
    
    @Override
    public Optional<LocalDate> getLastScheduleDayProcessed() {
        DBObject dbo = collection.findOne(new MongoQueryBuilder().idEquals(KEY).build());
        
        if (dbo == null) {
            return Optional.absent();
        }
        
        return Optional.of(TranslatorUtils.toLocalDate(dbo, DATE_KEY));
    }

    @Override
    public void setLastScheduleDayProcessed(LocalDate date) {
        DBObject dbo = new BasicDBObject();
        dbo.put(MongoConstants.ID, KEY);
        TranslatorUtils.fromLocalDate(dbo, DATE_KEY, date);
        collection.save(dbo);
    }

}
