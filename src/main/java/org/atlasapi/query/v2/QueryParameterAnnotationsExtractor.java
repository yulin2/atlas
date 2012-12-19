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
    
    private static final Set<String> annotationNames = ImmutableSet.copyOf(
        Iterables.transform(Annotation.all(), Annotation.toKeyFunction())
    );
    
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
        
        throw new IllegalArgumentException(invalidParamMessage(invalid));
        
    }
    
    private String invalidParamMessage(List<String> invalidParams) {
        StringBuilder msg = new StringBuilder("Invalid annotations: ");
        Iterator<String> iter = invalidParams.iterator();
        if (iter.hasNext()) {
            appendInvalidName(msg, iter.next());
            while(iter.hasNext()) {
                msg.append(", ");
                appendInvalidName(msg, iter.next());
            }
        }
        return msg.toString();
    }

    private void appendInvalidName(StringBuilder msg, String invalid) {
        msg.append(invalid);
        String suggestion = findSuggestion(invalid, annotationNames);
        if (suggestion != null) {
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
