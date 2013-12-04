package org.atlasapi.remotesite.five;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.http.HttpStatusCode;

@Controller
public class FiveBrandUpdateController {

    private final FiveUpdater fiveUpdater;
    
    public FiveBrandUpdateController(FiveUpdater fiveUpdater) {
        this.fiveUpdater = fiveUpdater;
    }
    
    @RequestMapping(value="/system/update/five/brand/{id}", method=RequestMethod.POST)
    public void updateBrand(HttpServletResponse response, @PathVariable("id") String id) {
        fiveUpdater.updateBrand(id);
        response.setStatus(HttpStatusCode.OK.code());
        response.setContentLength(0);
    }
    
}
