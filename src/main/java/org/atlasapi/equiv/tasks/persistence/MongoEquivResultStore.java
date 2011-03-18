package org.atlasapi.equiv.tasks.persistence;

import java.util.List;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.media.entity.Described;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoEquivResultStore implements EquivResultStore {
    
    private DBCollection results;
    private ContainerEquivResultTranslator equivTranslator;
    
    public MongoEquivResultStore(DatabasedMongo mongo) {
        this(mongo, new ContainerEquivResultTranslator());
    }

    public MongoEquivResultStore(DatabasedMongo mongo, ContainerEquivResultTranslator equivTranslator) {
        this.results = mongo.collection("equivlog");
        this.equivTranslator = equivTranslator;
    }

    @Override
    public <T extends Described, U extends Described> void store(ContainerEquivResult<T, U> result) {
        results.update(new BasicDBObject(MongoConstants.ID, result.described().getCanonicalUri()), equivTranslator.toDBObject(result), true, false);
    }

    @Override
    public ContainerEquivResult<String, String> resultFor(String canonicalUri) {
        return equivTranslator.fromDBObject(results.findOne(new BasicDBObject(MongoConstants.ID, canonicalUri)));
    }

    @Override
    public List<ContainerEquivResult<String, String>> results() {
        return ImmutableList.copyOf(Iterables.transform(results.find(), new Function<DBObject, ContainerEquivResult<String, String>>() {
            @Override
            public ContainerEquivResult<String, String> apply(DBObject input) {
                return equivTranslator.fromDBObject(input);
            }
        }));
    }

}
