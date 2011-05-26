package org.atlasapi.equiv.results.probe;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.concat;

import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.persistence.EquivalenceIdentifier;
import org.atlasapi.equiv.results.persistence.RestoredEquivalenceResult;
import org.eclipse.jetty.util.UrlEncoded;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.time.DateTimeZones;

public class EquivalenceProbeModelBuilder {

    public SimpleModel build(EquivalenceResultProbe input, RestoredEquivalenceResult result) {
        SimpleModel model = new SimpleModel();

        model.put("target", input.target()).put("encodedId", UrlEncoded.encodeString(input.target()));

        if (result == null) {
            return model;
        }

        model.put("title", result.title());
        model.put("timestamp", result.resultTime().toDateTime(DateTimeZones.LONDON).toString("YYYY-MM-dd HH:mm:ss"));

        Map<String, EquivalenceIdentifier> equivalenceIds = Maps.uniqueIndex(result.combinedResults().keySet(), new Function<EquivalenceIdentifier, String>() {
            @Override
            public String apply(EquivalenceIdentifier input) {
                return input.id();
            }
        });

        model.put("expected", modelForExpected(input.expectedEquivalent(), true, result, equivalenceIds));
        model.put("notExpected", modelForExpected(input.expectedNotEquivalent(), false, result, equivalenceIds));
        model.put("others", otherStrongEquivalents(copyOf(concat(input.expectedEquivalent(), input.expectedNotEquivalent())), equivalenceIds, result.combinedResults()));

        return model;
    }

    private SimpleModelList modelForExpected(Set<String> expectedEquivalents, boolean isExpected, RestoredEquivalenceResult result, Map<String, EquivalenceIdentifier> equivalenceIds) {
        SimpleModelList expecteds = new SimpleModelList();
        for (String expected : expectedEquivalents) {
            SimpleModel expectedModel = new SimpleModel().put("id", expected).put("encodedId", UrlEncoded.encodeString(expected));

            EquivalenceIdentifier expectedId = equivalenceIds.get(expected);
            boolean correctExpectation = false;
            if (isExpected && expectedId != null && expectedId.strong()) {
                correctExpectation = true;
            } else if (!isExpected && (expectedId == null || (expectedId != null && !expectedId.strong()))) {
                correctExpectation = true;
            }
            expectedModel.put("correctExpectation", correctExpectation);
            expectedModel.put("score", expectedId != null ? String.format("%+.3f", result.combinedResults().get(expectedId)) : "N/A");

            expecteds.add(expectedModel);
        }
        return expecteds;
    }

    private SimpleModelList otherStrongEquivalents(ImmutableSet<String> specified, Map<String, EquivalenceIdentifier> equivalenceIds, Map<EquivalenceIdentifier, Double> combinedResults) {
        SimpleModelList others = new SimpleModelList();

        for (EquivalenceIdentifier id : Maps.filterKeys(equivalenceIds, not(in(specified))).values()) {
            if (id.strong()) {
                SimpleModel model = new SimpleModel();
                model.put("id", id.id());
                model.put("encodedId", UrlEncoded.encodeString(id.id()));
                model.put("score", String.format("%+.3f", combinedResults.get(id)));
                others.add(model);
            }
        }

        return others;
    }

}
