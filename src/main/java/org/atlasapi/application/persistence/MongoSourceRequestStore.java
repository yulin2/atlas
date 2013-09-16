package org.atlasapi.application.persistence;

import java.util.Set;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.SourceRequest;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

public class MongoSourceRequestStore implements SourceRequestStore {

public static final String SOURCE_REQUESTS_COLLECTION = "sourceRequests";
    
    private final SourceRequestTranslator translator = new SourceRequestTranslator();
    
    private final DBCollection sourceRequests;
    
    private final Function<DBObject, SourceRequest> translatorFunction = new Function<DBObject, SourceRequest>(){
        @Override
        public SourceRequest apply(DBObject dbo) {
            return translator.fromDBObject(dbo);
        }
    };
    
    public MongoSourceRequestStore(DatabasedMongo mongo) {
        this.sourceRequests = mongo.collection(SOURCE_REQUESTS_COLLECTION);
    }
    @Override
    public void store(SourceRequest sourceRequest) {
        this.sourceRequests.save(translator.toDBObject(sourceRequest));
    }
    @Override
    public Optional<SourceRequest> getBy(Application application, Publisher source) {
        return Optional.fromNullable(translator.fromDBObject(this.sourceRequests.findOne(where().idEquals(translator.createKey(application, source)).build())));
    }
    @Override
    public Set<SourceRequest> sourceRequestsFor(Publisher publisher) {
        return ImmutableSet.copyOf(Iterables.transform(sourceRequests.find(where().fieldEquals(SourceRequestTranslator.SOURCE_KEY, publisher.key()).build()), translatorFunction));
    }
    @Override
    public Set<SourceRequest> all() {
        return ImmutableSet.copyOf(Iterables.transform(sourceRequests.find(), translatorFunction));
    }
}