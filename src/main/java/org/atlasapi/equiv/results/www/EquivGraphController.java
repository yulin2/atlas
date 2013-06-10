package org.atlasapi.equiv.results.www;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;

@Controller
public class EquivGraphController {
    
    private final LookupEntryStore lookupStore;

    public EquivGraphController(LookupEntryStore lookupStore) {
        this.lookupStore = checkNotNull(lookupStore);
    }
    
    @RequestMapping("/system/equivalence/graph")
    public String graphEquiv(Map<String,Object> model, @RequestParam("uri") String uri, 
            @RequestParam(value="min_edges", required=false, defaultValue="1") String minEdges) {
        model.put("uri", uri);
        model.put("min_edges", Integer.parseInt(minEdges));
        return "equivalence.graph";
    }
    
    @RequestMapping("/system/equivalence/graph/data.json")
    public String graphData(Map<String,Object> model, @RequestParam("uri") String uri, 
            @RequestParam(value="min_edges", required=false, defaultValue="1") String minEdges) {
        int minimumEdgeCount = Integer.parseInt(minEdges);
        model.put("uri", uri);
        model.put("min_edges", minimumEdgeCount);
        LookupEntry subj = Iterables.getOnlyElement(
            lookupStore.entriesForCanonicalUris(ImmutableList.of(uri)), 
            null
        );
        
        if (subj != null) {
            Iterable<LookupEntry> equivs = lookupStore.entriesForCanonicalUris(
                Iterables.transform(subj.equivalents(), LookupRef.TO_ID));
            
            List<SimpleModel> nodes = Lists.newLinkedList(); 

            for (LookupEntry equiv : equivs) {
                if (edgeCount(equiv) >= minimumEdgeCount) {
                    SimpleModel nodeModel = modelNode(equiv);
                    if (equiv.uri().equals(uri)) {
                        nodeModel.put("fixed", true);
                    }
                    nodes.add(nodeModel);
                }
            }
            
            model.put("content", new SimpleModelList(nodes));
        }
        
        return "equivalence.graph";
    }

    private int edgeCount(LookupEntry equiv) {
        return nonReflexiveIds(equiv, equiv.directEquivalents()).size()
                + nonReflexiveIds(equiv, equiv.explicitEquivalents()).size();
    }

    private Collection<String> nonReflexiveIds(LookupEntry equiv, Set<LookupRef> directEquivalents) {
        return Collections2.filter(
                Collections2.transform(directEquivalents, LookupRef.TO_ID),
                Predicates.not(Predicates.equalTo(equiv.uri())));
    }

    private SimpleModel modelNode(LookupEntry equiv) {
        return new SimpleModel()
            .put("uri", equiv.uri())
            .put("source", equiv.lookupRef().publisher().key())
            .putStrings("direct", nonReflexiveIds(equiv, equiv.directEquivalents()))
            .putStrings("explicit", nonReflexiveIds(equiv, equiv.explicitEquivalents()))
            ;
    }
    
}
