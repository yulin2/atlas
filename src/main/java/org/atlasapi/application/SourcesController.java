package org.atlasapi.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class SourcesController {

    /**
     * POST /4.0/sources/:sourceId/applications Updates permission for a source.
     * Post with app id and permission (read/write) required Params: "id":
     * "abc", "permission": "read"
     * 
     * @param request
     * @param response
     */
    @RequestMapping(value = "/4.0/sources/{sid}/applications", method = RequestMethod.POST)
    public void writeSourceForApplication(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * DELETE /4.0/sources/:sourceId/applications Removes a permission
     * (read/write) from an app on a source. Post with app id and permission
     * needed.
     */
    @RequestMapping(value = "/4.0/sources/{sid}/applications", method = RequestMethod.DELETE)
    public void deleteSourceForApplication(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * POST /4.0/sources/:sourceId/applications/readers/:appId/state Changes
     * state of app for source, e.g. "available", "requested". Params: "state":
     * "available"
     */
    public void changeSourceStateForApplication(HttpServletRequest request,
            HttpServletResponse response) {

    }
}
