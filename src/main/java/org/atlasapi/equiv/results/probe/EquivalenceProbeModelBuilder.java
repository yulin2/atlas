package org.atlasapi.equiv.results.probe;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.concat;

import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.persistence.CombinedEquivalenceScore;
import org.atlasapi.equiv.results.persistence.StoredEquivalenceResult;
import org.atlasapi.media.common.Id;
import org.eclipse.jetty.util.UrlEncoded;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.time.DateTimeZones;

public class EquivalenceProbeModelBuilder {

    public SimpleModel build(EquivalenceResultProbe input, StoredEquivalenceResult result) {
        SimpleModel model = new SimpleModel();

        model.putAsString("target", input.target().longValue())
            .put("encodedId", UrlEncoded.encodeString(input.target().toString()));

        if (result == null) {
            return model;
        }

        model.put("title", result.title());
        model.put("timestamp", result.resultTime().toDateTime(DateTimeZones.LONDON).toString("YYYY-MM-dd HH:mm:ss"));

        Map<Id, CombinedEquivalenceScore> equivalenceIds = Maps.uniqueIndex(result.combinedResults(), new Function<CombinedEquivalenceScore, Id>() {
            @Override
            public Id apply(CombinedEquivalenceScore input) {
                return input.id();
            }
        });

        model.put("expected", modelForExpected(input.expectedEquivalent(), true, equivalenceIds));
        model.put("notExpected", modelForExpected(input.expectedNotEquivalent(), false, equivalenceIds));
        model.put("others", otherStrongEquivalents(copyOf(concat(input.expectedEquivalent(), input.expectedNotEquivalent())), equivalenceIds));

        return model;
    }

    private SimpleModelList modelForExpected(Set<Id> expectedEquivalents, boolean isExpected, Map<Id, CombinedEquivalenceScore> equivalences) {
        SimpleModelList expecteds = new SimpleModelList();
        for (Id expected : expectedEquivalents) {
            SimpleModel expectedModel = new SimpleModel()
                .putAsString("id", expected.longValue())
                .put("encodedId", UrlEncoded.encodeString(expected.toString()));

            CombinedEquivalenceScore expectedEquivalence = equivalences.get(expected);
            boolean correctExpectation = false;
            if (isExpected && expectedEquivalence != null && expectedEquivalence.strong()) {
                correctExpectation = true;
            } else if (!isExpected && (expectedEquivalence == null || (expectedEquivalence != null && !expectedEquivalence.strong()))) {
                correctExpectation = true;
            }
            expectedModel.put("correctExpectation", correctExpectation);
            expectedModel.put("score", expectedEquivalence != null ? String.format("%+.3f", expectedEquivalence.score()) : "N/A");

            expecteds.add(expectedModel);
        }
        return expecteds;
    }

    private SimpleModelList otherStrongEquivalents(ImmutableSet<Id> ids, Map<Id, CombinedEquivalenceScore> equivalenceIds) {
        SimpleModelList others = new SimpleModelList();

        for (CombinedEquivalenceScore id : Maps.filterKeys(equivalenceIds, not(in(ids))).values()) {
            if (id.strong()) {
                SimpleModel model = new SimpleModel();
                model.putAsString("id", id.id().longValue());
                model.put("encodedId", UrlEncoded.encodeString(id.id().toString()));
                model.put("score", String.format("%+.3f", equivalenceIds.get(id).score()));
                others.add(model);
            }
        }

        return others;
    }

}
