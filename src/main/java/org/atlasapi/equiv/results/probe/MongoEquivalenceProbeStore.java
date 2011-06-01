package org.atlasapi.equiv.results.probe;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static org.atlasapi.equiv.results.probe.EquivalenceResultProbe.equivalenceResultProbeFor;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoEquivalenceProbeStore implements EquivalenceProbeStore {

    private DBCollection probeCollection;

    public MongoEquivalenceProbeStore(DatabasedMongo mongo) {
        this.probeCollection = mongo.collection("equivalenceProbes");
    }
    
    @Override
    public void store(EquivalenceResultProbe probe) {
        probeCollection.update(
            where().fieldEquals(ID, probe.target()).build(), 
            update().setField("expected", probe.expectedEquivalent()).setField("notExpected", probe.expectedNotEquivalent()).build(), 
            true, 
            false
        );
    }

    @Override
    public EquivalenceResultProbe probeFor(String canonicalUri) {
        DBObject probeDbo = probeCollection.findOne(canonicalUri);
        if(probeDbo == null) {
            return null;
        }
        return translate(probeDbo);
    }

    private EquivalenceResultProbe translate(DBObject probeDbo) {
        return equivalenceResultProbeFor(TranslatorUtils.toString(probeDbo,ID))
            .isEquivalentTo(TranslatorUtils.toList(probeDbo, "expected"))
            .isNotEquivalentTo(TranslatorUtils.toList(probeDbo, "notExpected")).build();
    }

    @Override
    public List<EquivalenceResultProbe> probes() {
        return ImmutableList.copyOf(Iterables.transform(probeCollection.find(), new Function<DBObject, EquivalenceResultProbe>() {
            @Override
            public EquivalenceResultProbe apply(DBObject input) {
                return translate(input);
            }
        }));
    }

}
