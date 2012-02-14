package org.atlasapi.query.v2;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.output.Annotation;

import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class QueryParameterAnnotationsExtractor {
    
    private final String parameterName;
    private final Splitter csvSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    public QueryParameterAnnotationsExtractor(String parameterName) {
        this.parameterName = parameterName;
    }
    
    public QueryParameterAnnotationsExtractor() {
        this("annotations");
    }

    public Optional<Set<Annotation>> extract(HttpServletRequest request) {
        
        String serialisedAnnotations = request.getParameter(parameterName);
        
        if(serialisedAnnotations == null) {
            return Optional.absent();
        }
        
        return Optional.<Set<Annotation>>of(ImmutableSet.copyOf(Iterables.transform(csvSplitter.split(serialisedAnnotations), Functions.forMap(Annotation.LOOKUP))));
        
    }
    
}
