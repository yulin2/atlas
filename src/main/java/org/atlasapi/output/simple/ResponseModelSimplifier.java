package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.youview.tasks.Response;
import org.atlasapi.output.Annotation;


public class ResponseModelSimplifier implements ModelSimplifier<Response, org.atlasapi.feeds.youview.tasks.simple.Response> {

    @Override
    public org.atlasapi.feeds.youview.tasks.simple.Response simplify(Response model,
            Set<Annotation> annotations, ApplicationConfiguration config) {
        org.atlasapi.feeds.youview.tasks.simple.Response response = new org.atlasapi.feeds.youview.tasks.simple.Response();
        
        response.setStatus(model.status());
        response.setCreated(model.created().toDate());
        response.setPayload(model.payload());
        
        return response;
    }

}
