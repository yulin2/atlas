package org.atlasapi.remotesite.bbc;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoProgressStore implements ProgressStore {

    private static final String COLLECTION = "scheduling";
    private static final String ROW_ID = "bbc-atoz";
    
    private final DBCollection collection;

    public MongoProgressStore(DatabasedMongo mongo) {
        this.collection = mongo.collection(COLLECTION);
    }
 
    @Override
    public void saveProgress(String channel, String pid) {
        collection.save(new BasicDBObjectBuilder().append(ID, ROW_ID).append("channel", channel).append("pid", pid).get());
    }
    
    @Override
    public void resetProgress() {
        collection.remove(new BasicDBObject(ID, ROW_ID));
    }
    
    @Override
    public ChannelAndPid getProgress() {
        DBObject dbo = collection.findOne(ROW_ID);
        if(dbo == null) {
            return null;
        }
        return new ChannelAndPid((String)dbo.get("channel"), (String)dbo.get("pid"));
    }
    
}
