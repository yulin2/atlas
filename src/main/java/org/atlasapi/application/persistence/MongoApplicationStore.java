package org.atlasapi.application.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;
import org.elasticsearch.common.Preconditions;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;

public class MongoApplicationStore implements ApplicationStore {

    public static final String APPLICATION_COLLECTION = "applications";
    private final DBCollection applications;
    private final MongoApplicationTranslator translator = new MongoApplicationTranslator();

    private final Function<DBObject, Application> translatorFunction = new Function<DBObject, Application>() {

        @Override
        public Application apply(DBObject dbo) {
            return translator.fromDBObject(dbo);
        }
    };

    public MongoApplicationStore(DatabasedMongo adminMongo) {
        this.applications = adminMongo.collection(APPLICATION_COLLECTION);
        this.applications.setReadPreference(ReadPreference.primary());
    }

    @Override
    public Iterable<Application> allApplications() {
        return Iterables.transform(applications.find(where().build()), translatorFunction);
    }

    @Override
    public Optional<Application> applicationFor(Id id) {
        return Optional.fromNullable(translator.fromDBObject(
                applications.findOne(
                        where().fieldEquals(MongoApplicationTranslator.DEER_ID_KEY, id.longValue())
                                .build())
                )
                );
    }

    @Override
    public void store(Application application) {
        Preconditions.checkNotNull(application);
        applications.save(translator.toDBObject(application));
    }
}
