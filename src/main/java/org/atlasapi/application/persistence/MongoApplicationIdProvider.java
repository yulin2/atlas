package org.atlasapi.application.persistence;

import org.atlasapi.media.common.Id;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.ReadPreference;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

public class MongoApplicationIdProvider implements ApplicationIdProvider {

    public static final String ID_COLLECTION = "id";
    public static final String APPLICATION_ENTRY_ID = "application";
    private final MongoApplicationIdTranslator idTranslator = new MongoApplicationIdTranslator();

    private final DBCollection ids;

    public MongoApplicationIdProvider(DatabasedMongo adminMongo) {
        this.ids = adminMongo.collection(ID_COLLECTION);
        this.ids.setReadPreference(ReadPreference.primary());
    }

    @Override
    public Id issueNextId() {
        Id id = idTranslator.fromDBObject(ids.findOne(where().idEquals(APPLICATION_ENTRY_ID)
                .build()));
        Id newId = Id.valueOf(id.longValue() + 1);
        ids.save(idTranslator.toDBObject(newId));
        return id;
    }

}
