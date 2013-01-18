package org.atlasapi.equiv.results.www;

import static com.metabroadcast.common.http.HttpStatusCode.NOT_FOUND;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.StoredEquivalenceResult;
import org.atlasapi.equiv.results.probe.EquivalenceProbeStore;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;

@Controller
public class EquivalenceResultController {

    private final EquivalenceResultStore store;
    private final EquivalenceProbeStore probeStore;

    private final RestoredEquivalenceResultModelBuilder resultModelBuilder;
    private final ContentResolver contentResolver;

    public EquivalenceResultController(EquivalenceResultStore store, EquivalenceProbeStore probeStore, ContentResolver contentResolver) {
        this.store = store;
        this.probeStore = probeStore;
        this.contentResolver = contentResolver;
        this.resultModelBuilder = new RestoredEquivalenceResultModelBuilder();
    }

    @RequestMapping(value = "/system/equivalence/result", method = RequestMethod.GET)
    public String showResult(Map<String, Object> model, HttpServletResponse response, @RequestParam(value = "uri", required = true) String uri) throws IOException {

        StoredEquivalenceResult equivalenceResult = store.forId(Id.valueOf(uri));

        if (equivalenceResult == null) {
            response.sendError(NOT_FOUND.code(), "No result for URI");
            return null;
        }

        SimpleModel resultModel = resultModelBuilder.build(equivalenceResult, probeStore.probeFor(uri));

        model.put("result", resultModel);

        return "equivalence.result";
    }

    @RequestMapping(value = "/system/equivalence/results", method = RequestMethod.GET)
    public String showSubResults(Map<String, Object> model, HttpServletResponse response, @RequestParam(value = "uri", required = true) String uri) throws IOException {

        Maybe<Identified> ided = contentResolver.findByCanonicalUris(ImmutableList.of(uri)).getFirstValue();

        if (ided.isNothing()) {
            response.sendError(NOT_FOUND.code(), "Unknown URI");
            return null;
        }

        SimpleModelList resultModelList = new SimpleModelList();

        if (ided.requireValue() instanceof Container) {

            List<StoredEquivalenceResult> results = store.forIds(Iterables.transform(((Container) ided.requireValue()).getChildRefs(), new Function<ChildRef, Id>() {
                @Override
                public Id apply(ChildRef input) {
                    return input.getId();
                }
            }));

            for (StoredEquivalenceResult result : results) {
                resultModelList.add(resultModelBuilder.build(result, probeStore.probeFor(uri)));
            }

        }
        model.put("results", resultModelList);
        return "equivalence.results";
    }
}
