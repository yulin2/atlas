package org.atlasapi.equiv.results.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;

public class MongoEquivalenceResultStore implements EquivalenceResultStore {

    private DBCollection equivResults;
    private EquivalenceResultTranslator translator;

    public MongoEquivalenceResultStore(DatabasedMongo mongo) {
        this.equivResults = mongo.collection("equivResults");
        this.translator = new EquivalenceResultTranslator();
    }
    
    @Override
    public <T extends Content> void store(EquivalenceResult<T> result) {
        equivResults.update(where().fieldEquals(ID, result.target().getCanonicalUri()).build(), translator.toDBObject(result), true, false);
    }

    @Override
    public RestoredEquivalenceResult forId(String canonicalUri) {
        return translator.fromDBObject(equivResults.findOne(canonicalUri));
    }

}
