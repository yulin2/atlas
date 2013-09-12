package org.atlasapi.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;

public class SourcesController {
    private final ApplicationUpdater applicationUpdater;
    
    
    public SourcesController(ApplicationUpdater applicationUpdater) {
        this.applicationUpdater = applicationUpdater;
    }

    /**
     * POST /4.0/sources/:sourceId/applications Updates permission for a source.
     * Post with app id and permission (read/write) required Params: "id":
     * "abc", "permission": "read"
     * 
     * @param request
     * @param response
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications", method = RequestMethod.POST)
    public void writeSourceForApplication(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String permission) {
        

    }

    /**
     * DELETE /4.0/sources/:sourceId/applications Removes a permission
     * (read/write) from an app on a source. Post with app id and permission
     * needed.
     */
    @RequestMapping(value = "/4.0/sources/{sid}/applications", method = RequestMethod.DELETE)
    public void deleteSourceForApplication(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String permission) {

    }

    /**
     * POST /4.0/sources/:sourceId/applications/readers/:appId/state Changes
     * state of app for source, e.g. "available", "requested". Params: "state":
     * "available"
     * @throws Exception 
     */
    public void changeSourceStateForApplication(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String state) throws Exception {
        
        Optional<Publisher> source = applicationUpdater.decodeSourceId(sourceId);
        if (source.isPresent()) {
            Id applicationId = applicationUpdater.decode(id);
            SourceState requestedState = SourceState.valueOf(state);
            applicationUpdater.updateSourceState(applicationId, source.get(), requestedState);
        } else {
            throw new NotFoundException(null);
        }
    }
}
