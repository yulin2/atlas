package org.atlasapi.equiv.results.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.content.Content;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoEquivalenceResultStore implements EquivalenceResultStore {

    private DBCollection equivResults;
    private EquivalenceResultTranslator translator;

    public MongoEquivalenceResultStore(DatabasedMongo mongo) {
        this.equivResults = mongo.collection("equivalence");
        this.translator = new EquivalenceResultTranslator();
    }
    
    @Override
    public <T extends Content> StoredEquivalenceResult store(EquivalenceResult<T> result) {
        DBObject dbo = translator.toDBObject(result);
        equivResults.update(where().fieldEquals(ID, result.subject().getCanonicalUri()).build(), dbo, true, false);
        return translator.fromDBObject(dbo);
    }

    @Override
    public StoredEquivalenceResult forId(String canonicalUri) {
        return translator.fromDBObject(equivResults.findOne(canonicalUri));
    }

    @Override
    public List<StoredEquivalenceResult> forIds(Iterable<String> canonicalUris) {
        return ImmutableList.copyOf(Iterables.transform(equivResults.find(where().idIn(canonicalUris).build()), new Function<DBObject, StoredEquivalenceResult>() {

            @Override
            public StoredEquivalenceResult apply(DBObject input) {
                return translator.fromDBObject(input);
            }
        }));
    }

}
