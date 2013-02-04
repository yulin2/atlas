package org.atlasapi.query.common;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.output.Annotation;

import com.google.common.base.Optional;

public interface AnnotationsExtractor {

    Optional<Set<Annotation>> extractFromRequest(HttpServletRequest request);

}