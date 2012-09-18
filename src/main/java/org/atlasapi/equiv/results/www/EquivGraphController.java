package org.atlasapi.equiv.results.www;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Functions;
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
    public String graphEquiv(Map<String,Object> model, @RequestParam("uri") String uri) {
        model.put("uri", uri);
        return "equivalence.graph";
    }
    
    @RequestMapping("/system/equivalence/graph/data.json")
    public String graphData(Map<String,Object> model, @RequestParam("uri") String uri) {
        model.put("uri", uri);
        LookupEntry subj = Iterables.getOnlyElement(
            lookupStore.entriesForCanonicalUris(ImmutableList.of(uri)), 
            null
        );
        
        if (subj != null) {
            Iterable<LookupEntry> equivs = lookupStore.entriesForIds(
                Iterables.transform(subj.equivalents(), LookupRef.TO_ID));
            
            List<SimpleModel> nodes = Lists.newLinkedList(); 

            int i = 0;
            for (LookupEntry equiv : equivs) {
                SimpleModel nodeModel = modelNode(equiv, i++);
                if (equiv.uri().equals(uri)) {
                    nodeModel.put("fixed", true);
                }
                nodes.add(nodeModel);
            }
            
            model.put("content", new SimpleModelList(nodes));
        }
        
        return "equivalence.graph";
    }

    private SimpleModel modelNode(LookupEntry equiv, int index) {
        return new SimpleModel()
            .put("uri", equiv.uri())
            .put("source", equiv.lookupRef().publisher().key())
            .putStrings("direct", Collections2.filter(
                    Collections2.transform(equiv.directEquivalents(), Functions.compose(Functions.toStringFunction(), LookupRef.TO_ID)),
                    Predicates.not(Predicates.equalTo(equiv.uri()))
            ))
            .putStrings("explicit", Collections2.filter(
                    Collections2.transform(equiv.explicitEquivalents(),  Functions.compose(Functions.toStringFunction(), LookupRef.TO_ID)),
                    Predicates.not(Predicates.equalTo(equiv.uri()))
            ));
    }
    
}
