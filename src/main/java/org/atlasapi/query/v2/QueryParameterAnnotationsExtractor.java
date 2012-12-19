package org.atlasapi.query.v2;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.output.Annotation;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class QueryParameterAnnotationsExtractor {
    
    private static final Set<String> annotationKeys = ImmutableSet.copyOf(
        Iterables.transform(Annotation.all(), Annotation.toKeyFunction())
    );
    
    private static final Set<String> annotationNames = ImmutableSet.copyOf(
        Iterables.transform(Annotation.all(), Annotation.toRequestName())
    );
    
    private final String parameterName;
    private final Splitter csvSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    public QueryParameterAnnotationsExtractor(String parameterName) {
        this.parameterName = parameterName;
    }
    
    public QueryParameterAnnotationsExtractor() {
        this("annotations");
    }
    
    public Optional<Set<Annotation>> extractFromRequest(HttpServletRequest request, Optional<String> context) {
        
        String serialisedAnnotations = request.getParameter(parameterName);
        
        if(serialisedAnnotations == null) {
            return Optional.absent();
        }
        
        List<String> invalid = Lists.newLinkedList();
        Builder<Annotation> annotations = ImmutableSet.builder();
        for (String annotationKey : csvSplitter.split(serialisedAnnotations)) {
            Optional<Annotation> possibleAnnotation = Annotation.fromRequestName(annotationKey, context);
            if (possibleAnnotation.isPresent()) {
                annotations.add(possibleAnnotation.get());
            } else {
                invalid.add(annotationKey);
            }
        }
        
        if (invalid.isEmpty()) {
            return Optional.<Set<Annotation>>of(annotations.build());
        }
        
        throw new IllegalArgumentException(invalidParamMessage(invalid, annotationNames, context));
        
    }

    public Optional<Set<Annotation>> extractFromKeys(HttpServletRequest request) {
        
        String serialisedAnnotations = request.getParameter(parameterName);
        
        if(serialisedAnnotations == null) {
            return Optional.absent();
        }
        
        List<String> invalid = Lists.newLinkedList();
        Builder<Annotation> annotations = ImmutableSet.builder();
        for (String annotationKey : csvSplitter.split(serialisedAnnotations)) {
            Optional<Annotation> possibleAnnotation = Annotation.fromKey(annotationKey);
            if (possibleAnnotation.isPresent()) {
                annotations.add(possibleAnnotation.get());
            } else {
                invalid.add(annotationKey);
            }
        }
        
        if (invalid.isEmpty()) {
            return Optional.<Set<Annotation>>of(annotations.build());
        }
        
        throw new IllegalArgumentException(invalidParamMessage(invalid, annotationKeys, Optional.<String>absent()));
        
    }
    
    private String invalidParamMessage(List<String> invalidParams, Set<String> valid, Optional<String> context) {
        StringBuilder msg = new StringBuilder("Invalid annotations: ");
        Iterator<String> iter = invalidParams.iterator();
        if (iter.hasNext()) {
            appendInvalidName(msg, iter.next(), valid, context);
            while(iter.hasNext()) {
                msg.append(", ");
                appendInvalidName(msg, iter.next(), valid, context);
            }
        }
        return msg.toString();
    }

    private void appendInvalidName(StringBuilder msg, String invalid, Set<String> valid, Optional<String> context) {
        msg.append(invalid);
        String suggestion = findSuggestion(Annotation.requestNameForContext(invalid, context), valid);
        if (suggestion != null) {
            if (context.isPresent()) {
                suggestion = suggestion.substring(context.get().length()+1);
            }
            msg.append(" (did you mean ").append(suggestion).append("?)");
        }
    }

    private String findSuggestion(String invalid, Set<String> validParams) {
        for (String valid : validParams) {
            int distance = StringUtils.getLevenshteinDistance(valid, invalid);
            int maxDistance = 2;
            if (distance < maxDistance) {
                return valid;
            }
        }
        return null;
    }
    
}
