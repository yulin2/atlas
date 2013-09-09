package org.atlasapi.application.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;


public class MongoApplicationStore2 implements ApplicationStore2 {
    public static final String APPLICATION_COLLECTION = "applications";
    private final DBCollection applications;
    private final DatabasedMongo adminMongo;
    private final MongoApplicationTranslator translator = new MongoApplicationTranslator();
    
    private final Function<DBObject, Application> translatorFunction = new Function<DBObject, Application>(){
        @Override
        public Application apply(DBObject dbo) {
            return translator.fromDBObject(dbo);
        }
    };
    
    public MongoApplicationStore2(DatabasedMongo adminMongo) {
        this.applications = adminMongo.collection(APPLICATION_COLLECTION);
        this.applications.setReadPreference(ReadPreference.primary());
        this.adminMongo = adminMongo;
    }

    @Override
    public Iterable<Application> allApplications() {
        return Iterables.transform(applications.find(where().build()), translatorFunction);
    }

    @Override
    public Optional<Application> applicationFor(Id id) {
        System.out.println(applications.getDB().getName());
        return Optional.fromNullable(translator.fromDBObject(
                   applications.findOne(
                           where().fieldEquals(MongoApplicationTranslator.DEER_ID_KEY, id.longValue()).build())
                   )
               );
    }

}
