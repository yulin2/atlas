package org.atlasapi.equiv.results.probe;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static org.atlasapi.equiv.results.probe.EquivalenceResultProbe.equivalenceResultProbeFor;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.common.Id;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoEquivalenceProbeStore implements EquivalenceProbeStore {

    private DBCollection probeCollection;

    public MongoEquivalenceProbeStore(DatabasedMongo mongo) {
        this.probeCollection = mongo.collection("equivalenceProbes");
    }
    
    @Override
    public void store(EquivalenceResultProbe probe) {
        Set<Id> expectedEquivalent = probe.expectedEquivalent();
        probeCollection.update(
            where().fieldEquals(ID, probe.target().longValue()).build(), 
            update()
                .setField("expected", toLongIds(expectedEquivalent))
                .setField("notExpected", toLongIds(probe.expectedNotEquivalent()))
                .build(), 
            true, 
            false
        );
    }

    private BasicDBList toLongIds(Set<Id> expectedEquivalent) {
        BasicDBList list = new BasicDBList();
        Iterables.addAll(list, Iterables.transform(expectedEquivalent, Id.toLongValue()));
        return list;
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
        String field = "expected";
        return equivalenceResultProbeFor(Id.valueOf(TranslatorUtils.toLong(probeDbo,ID)))
            .isEquivalentTo(fromLongIds(probeDbo, field))
            .isNotEquivalentTo(fromLongIds(probeDbo, "notExpected"))
            .build();
    }

    @SuppressWarnings("unchecked")
    private Iterable<Id> fromLongIds(DBObject probeDbo, String field) {
        return Iterables.transform((Iterable<Long>)probeDbo.get(field), Id.fromLongValue());
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
