package org.atlasapi.query.common;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableSet;

public interface AnnotationsExtractor {

    ActiveAnnotations extractFromRequest(HttpServletRequest request)
        throws InvalidAnnotationException;
    
    ImmutableSet<String> getParameterNames();

}