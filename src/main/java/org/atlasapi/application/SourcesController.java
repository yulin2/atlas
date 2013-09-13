package org.atlasapi.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.Permission;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.common.QueryExecutionException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;

@Controller
public class SourcesController {
    private final ApplicationUpdater applicationUpdater;
    private final AdminHelper adminHelper;
    
    public SourcesController(ApplicationUpdater applicationUpdater, AdminHelper adminHelper) {
        this.applicationUpdater = applicationUpdater;
        this.adminHelper = adminHelper;
    }

    /**
     * POST /4.0/sources/:sourceId/applications Updates permission for a source.
     * Post with app id and permission (read/write) required Params: "id":
     * "abc", "permission": "read"
     * @throws NotFoundException 
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications", method = RequestMethod.POST)
    public void writeSourceForApplication(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String permission) throws NotFoundException {
        Optional<Publisher> source = adminHelper.decodeSourceId(sourceId);
        if (source.isPresent()) {
            Id applicationId = adminHelper.decode(id);
            Permission permissionType = Permission.valueOf(permission.toUpperCase());
            Application existing = applicationUpdater.applicationFor(applicationId);
            Application modified = null;
            if (permissionType.equals(Permission.READ)) {
                modified = applicationUpdater.enableSource(existing, source.get());
            } else if (permissionType.equals(Permission.WRITE)) {
                modified = applicationUpdater.addWrites(existing, source.get());
            }
            if (modified != null) {
                applicationUpdater.storeApplication(modified);
            }
        } else {
            throw new NotFoundException(null);
        }
    }

    /**
     * DELETE /4.0/sources/:sourceId/applications Removes a permission
     * (read/write) from an app on a source. Post with app id and permission
     * needed.
     * @throws QueryExecutionException 
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications", method = RequestMethod.DELETE)
    public void deleteSourceForApplication(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String permission) throws QueryExecutionException {
        Optional<Publisher> source = adminHelper.decodeSourceId(sourceId);
        if (source.isPresent()) {
            Id applicationId = adminHelper.decode(id);
            Permission permissionType = Permission.valueOf(permission.toUpperCase());
            Application existing = applicationUpdater.applicationFor(applicationId);
            Application modified = null;
            if (permissionType.equals(Permission.READ)) {
                modified = applicationUpdater.disableSource(existing, source.get());
            } else if (permissionType.equals(Permission.WRITE)) {
                modified = applicationUpdater.removeWrites(existing, source.get());
            }
            if (modified != null) {
                applicationUpdater.storeApplication(modified);
            }
        } else {
            throw new QueryExecutionException("No source with id " + sourceId);
        }
    }

    /**
     * POST /4.0/sources/:sourceId/applications/readers/:appId/state Changes
     * state of app for source, e.g. "available", "requested". Params: "state":
     * "available"
     * @throws Exception 
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications/readers/{id}/state", method = RequestMethod.POST)
    public void changeSourceStateForApplication(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String sourceId,
            @PathVariable String id,
            @RequestParam String state) throws QueryExecutionException {
        
        Optional<Publisher> source = adminHelper.decodeSourceId(sourceId);
        if (source.isPresent()) {
            Id applicationId = adminHelper.decode(id);
            SourceState requestedState = SourceState.valueOf(state.toUpperCase());
            Application existing = applicationUpdater.applicationFor(applicationId);
            applicationUpdater.storeApplication(
                    applicationUpdater.changeReadSourceState(existing, source.get(), requestedState));
        } else {
            throw new QueryExecutionException("No source with id " + sourceId);
        }
    }
}
