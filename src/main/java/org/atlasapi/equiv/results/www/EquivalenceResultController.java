package org.atlasapi.equiv.results.www;

import static com.metabroadcast.common.http.HttpStatusCode.NOT_FOUND;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RestoredEquivalenceResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EquivalenceResultController {

    private final EquivalenceResultStore store;
    private final RestoredEquivalenceResultModelBuilder resultModelBuilder;

    public EquivalenceResultController(EquivalenceResultStore store) {
        this.store = store;
        this.resultModelBuilder = new RestoredEquivalenceResultModelBuilder();
    }
    
    @RequestMapping(value = "/system/equivalence/result", method = RequestMethod.GET)
    public String showResult(Map<String, Object> model, HttpServletResponse response, @RequestParam(value = "uri", required = true) String uri) throws IOException {
        
        RestoredEquivalenceResult equivalenceResult = store.forId(uri);
        
        if(equivalenceResult == null) {
            response.sendError(NOT_FOUND.code(), "No result for URI");
            return null;
        }
        
        model.put("result", resultModelBuilder.build(equivalenceResult));
        
        return "equivalence.result";
    }
    
}
