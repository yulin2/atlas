package org.atlasapi.equiv.results.probe;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.concat;
import static org.atlasapi.equiv.results.probe.EquivalenceResultProbe.equivalenceResultProbeFor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.equiv.results.persistence.EquivalenceIdentifier;
import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RestoredEquivalenceResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class EquivalenceResultProbeController {

    private final EquivalenceResultStore resultStore;
    private final EquivalenceProbeStore probeStore;
    private final Splitter csv = Splitter.on(",").omitEmptyStrings().trimResults();

    public EquivalenceResultProbeController(EquivalenceResultStore resultStore, EquivalenceProbeStore probeStore) {
        this.resultStore = resultStore;
        this.probeStore = probeStore;
    }
    
    @RequestMapping("system/equivalence/probes")
    public String showAllProbes(Map<String,Object> model, HttpServletResponse response, @RequestParam(value="uris",required=false,defaultValue="all") String uris) {
        
        Iterable<EquivalenceResultProbe> probes = "all".equals(uris) ? probeStore.probes() : probesIn(csv.split(uris));
        model.put("probes", modelsFor(probes).asListOfMaps());
        
        return "equivalence.probes";
    }

    private List<EquivalenceResultProbe> probesIn(Iterable<String> uris) {
        return ImmutableList.copyOf(Iterables.transform(uris, new Function<String, EquivalenceResultProbe>() {
            @Override
            public EquivalenceResultProbe apply(String input) {
                return probeStore.probeFor(input);
            }
        }));
    }

    private SimpleModelList modelsFor(Iterable<EquivalenceResultProbe> probes) {
        return new SimpleModelList(Iterables.transform(probes, new Function<EquivalenceResultProbe, SimpleModel>() {
            @Override
            public SimpleModel apply(EquivalenceResultProbe input) {
                return modelFor(input);
            }
        }));
    }

    private SimpleModel modelFor(EquivalenceResultProbe input) {
        SimpleModel model = new SimpleModel();
        
        model.put("target", input.target());
        
        RestoredEquivalenceResult result = resultStore.forId(input.target());

        if(result == null) {
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
        model.put("others", otherStrongEquivalents(copyOf(concat(input.expectedEquivalent(),input.expectedNotEquivalent())), equivalenceIds, result.combinedResults()));
        
        return model;
    }

    private SimpleModelList otherStrongEquivalents(ImmutableSet<String> specified, Map<String, EquivalenceIdentifier> equivalenceIds, Map<EquivalenceIdentifier, Double> combinedResults) {
        SimpleModelList others = new SimpleModelList();
        
        for (EquivalenceIdentifier id : Maps.filterKeys(equivalenceIds, not(in(specified))).values()) {
            if(id.strong()) {
                SimpleModel model = new SimpleModel();
                model.put("id", id.id());
                model.put("score", String.format("%+.3f",combinedResults.get(id)));
                others.add(model);
            }
        }
        
        return others;
    }

    private SimpleModelList modelForExpected(Set<String> expectedEquivalents, boolean isExpected, RestoredEquivalenceResult result, Map<String, EquivalenceIdentifier> equivalenceIds) {
        SimpleModelList expecteds = new SimpleModelList();
        for (String expected : expectedEquivalents) {
            SimpleModel expectedModel = new SimpleModel().put("id", expected);

            EquivalenceIdentifier expectedId = equivalenceIds.get(expected);
            boolean correctExpectation = false;
            if(isExpected && expectedId != null && expectedId.strong()) {
                correctExpectation = true;
            } else if (!isExpected && (expectedId == null || (expectedId != null && !expectedId.strong()))) {
                correctExpectation = true;
            }
            expectedModel.put("correctExpectation", correctExpectation);
            expectedModel.put("score", expectedId != null ? String.format("%+.3f",result.combinedResults().get(expectedId)) : "N/A");

            expecteds.add(expectedModel);
        }
        return expecteds;
    }
    
    @RequestMapping(value="system/equivalence/probes/update",method=RequestMethod.POST)
    public String updateProbe(Map<String,Object> model, HttpServletResponse response, @RequestParam("uri") String target, @RequestParam("expect") String expect, @RequestParam("notExpect") String notExpect) {
        
        EquivalenceResultProbe probe = equivalenceResultProbeFor(target).isEquivalentTo(csv.split(expect)).isNotEquivalentTo(csv.split(notExpect)).build();
        
        probeStore.store(probe);
        
        model.put("probe", modelFor(probe));
        
        return "equivalence.widgets.probe";
    }
    
    @RequestMapping(value = "system/equivalence/probes/update", method = RequestMethod.GET)
    public String configureProbe(Map<String, Object> model, HttpServletResponse response, @RequestParam(value = "uri", required = false) String target) throws IOException {

        target = Strings.nullToEmpty(target);
        
        SimpleModel probeModel = new SimpleModel().put("target", target);
        
        EquivalenceResultProbe probe = probeStore.probeFor(target);

        probeModel.put("expectedEquivalent", probe == null ? "" : Joiner.on(",\n").join(probe.expectedEquivalent()));
        probeModel.put("expectedNotEquivalent", probe == null ? "" : Joiner.on(",\n").join(probe.expectedNotEquivalent()));
        model.put("probe", probeModel);

        return "equivalence.probeUpdate";
    }
}
