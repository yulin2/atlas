package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.atlasapi.output.Annotation;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class QueryParameterAnnotationsExtractor implements AnnotationsExtractor {
    
    private static final class Contextualize implements Function<String, String> {

        private Optional<String> context;

        public Contextualize(Optional<String> context) {
            this.context = context;
        }

        @Override
        public String apply(String input) {
            return Annotation.requestNameForContext(input, context);
        }
    }

    private static final class ContextPrependTransform implements Function<String, String> {

        private Optional<String> context;

        public ContextPrependTransform(Optional<String> context) {
            this.context = context;
        }

        @Override
        public String apply(String input) {
            if (context.isPresent()) {
                input = input.substring(context.get().length()+1);
            }
            return input;
        }
    }

    private static final Set<String> annotationNames = ImmutableSet.copyOf(
        Iterables.transform(Annotation.all(), Annotation.toRequestName())
    );
    
    private final Splitter csvSplitter = Splitter.on(",").omitEmptyStrings().trimResults();

    private final String parameterName;
    private final Optional<String> context;
    private final ReplacementSuggestion replacmentSuggestion;

    public QueryParameterAnnotationsExtractor(String parameterName, @Nullable String context) {
        this.parameterName = checkNotNull(parameterName);
        this.context = Optional.fromNullable(context);
        this.replacmentSuggestion = new ReplacementSuggestion(annotationNames, 
            "Invalid annotations: ", " (did you mean %s?)", 
            new Contextualize(this.context), new ContextPrependTransform(this.context));
    }
    
    public QueryParameterAnnotationsExtractor(@Nullable String context) {
        this("annotations", context);
    }

    public QueryParameterAnnotationsExtractor() {
        this("annotations", null);
    }
    
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.of(this.parameterName);
    }
    
    @Override
    public Optional<Set<Annotation>> extractFromRequest(HttpServletRequest request) {
        
        String serialisedAnnotations = request.getParameter(parameterName);
        
        if(serialisedAnnotations == null) {
            return Optional.absent();
        }
        
        List<String> invalid = Lists.newLinkedList();
        ImmutableSet.Builder<Annotation> annotations = ImmutableSet.builder();
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
        
        throw new IllegalArgumentException(replacmentSuggestion.forInvalid(invalid));
        
    }

    @Deprecated
    public Optional<Set<Annotation>> extractFromKeys(HttpServletRequest request) {
        
        String serialisedAnnotations = request.getParameter(parameterName);
        
        if(serialisedAnnotations == null) {
            return Optional.absent();
        }
        
        List<String> invalid = Lists.newLinkedList();
        ImmutableSet.Builder<Annotation> annotations = ImmutableSet.builder();
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
        
        throw new IllegalArgumentException(replacmentSuggestion.forInvalid(invalid));
        
    }
    
}
