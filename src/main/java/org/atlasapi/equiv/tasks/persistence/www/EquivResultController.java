package org.atlasapi.equiv.tasks.persistence.www;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.equiv.tasks.persistence.EquivResultStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.model.ModelBuilder;

@Controller
public class EquivResultController {

    private final EquivResultStore store;
    private ModelBuilder<EquivResult<String>> resultModelBuilder = new EquivResultModelBuilder();

    public EquivResultController(EquivResultStore store) {
        this.store = store;
    }

    @RequestMapping(value = "/system/equivalence/results", method = RequestMethod.GET)
    public String viewResult(Map<String, Object> model, @RequestParam(value = "uri", required = true) String brandUri, HttpServletResponse response) {
        ContainerEquivResult<String, String> result = store.resultFor(brandUri);
        if (result == null) {
            response.setStatus(HttpStatusCode.NOT_FOUND.code());
            response.setContentLength(0);
            return null;
        }

        model.put("result", resultModelBuilder.build(result));
        return "system/equivalence/result";
    }
}
