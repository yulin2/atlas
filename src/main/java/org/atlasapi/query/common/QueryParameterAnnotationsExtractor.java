package org.atlasapi.query.common;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.output.Annotation;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class QueryParameterAnnotationsExtractor implements AnnotationsExtractor {
    
    private static final Set<String> annotationKeys = ImmutableSet.copyOf(
        Iterables.transform(Annotation.all(), Annotation.toKeyFunction())
    );
    
    private static final Set<String> annotationNames = ImmutableSet.copyOf(
        Iterables.transform(Annotation.all(), Annotation.toRequestName())
    );
    
    private final Splitter csvSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    private final String parameterName;
    private final Optional<String> context;

    public QueryParameterAnnotationsExtractor(String parameterName, @Nullable String context) {
        this.parameterName = parameterName;
        this.context = Optional.fromNullable(context);
    }
    
    public QueryParameterAnnotationsExtractor(@Nullable String context) {
        this("annotations", context);
    }

    public QueryParameterAnnotationsExtractor() {
        this("annotations", null);
    }
    
    @Override
    public Optional<Set<Annotation>> extractFromRequest(HttpServletRequest request) {
        
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

    @Deprecated
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
