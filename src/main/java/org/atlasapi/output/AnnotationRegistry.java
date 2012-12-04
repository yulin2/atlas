package org.atlasapi.output;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.atlasapi.output.annotation.OutputAnnotation;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class AnnotationRegistry {
    
    public static final Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private final BiMap<Annotation, OutputAnnotation<?>> annotationMap = HashBiMap.create();
        private final ImmutableSetMultimap.Builder<OutputAnnotation<?>, OutputAnnotation<?>> implications = ImmutableSetMultimap.builder();
        private final ImmutableSetMultimap.Builder<OutputAnnotation<?>, OutputAnnotation<?>> overrides = ImmutableSetMultimap.builder();
        
        public Builder register(Annotation annotation, OutputAnnotation<?> output) {
            annotationMap.put(annotation, output);
            return this;
        }
        
        public Builder register(Annotation annotation, OutputAnnotation<?> output, Iterable<Annotation> implieds){
            checkRegistered(implieds, "Cannot imply un-registered annotation '%s'");
            register(annotation, output);
            for (Annotation implied : implieds) {
                OutputAnnotation<?> impliedOut = annotationMap.get(implied);
                Preconditions.checkArgument(impliedOut.appliesTo(output.getAppliesTo()), "Implied annotation %s must apply", implied);
                implications.put(output, impliedOut);
            }
            return this;
        }
        
        public Builder register(Annotation annotation, OutputAnnotation<?> output, Iterable<Annotation> implieds, Iterable<Annotation> overridden) {
            checkRegistered(overridden, "Cannot override un-registered annotation '%s'");
            register(annotation, output, implieds);
            overrides.putAll(output, toOutput(overridden));
            return this;
        }
        
        public AnnotationRegistry build() {
            return new AnnotationRegistry(annotationMap, implications.build(), overrides.build());
        }
        
        private Iterable<OutputAnnotation<?>> toOutput(Iterable<Annotation> annotations) {
            return Iterables.transform(annotations, Functions.forMap(annotationMap));
        }

        private void checkRegistered(Iterable<Annotation> annotations, String errMsg) {
            for (Annotation annotation : annotations) {
                Preconditions.checkArgument(annotationMap.containsKey(annotation), errMsg, annotation);
            }
        }
    }
    

    private final SetMultimap<OutputAnnotation<?>, OutputAnnotation<?>> implications;
    private final SetMultimap<OutputAnnotation<?>, OutputAnnotation<?>> overrides;
    private final BiMap<Annotation, OutputAnnotation<?>> annotationMap;
    private final Ordering<OutputAnnotation<?>> ordering;

    private AnnotationRegistry(BiMap<Annotation, OutputAnnotation<?>> annotationMap,
        SetMultimap<OutputAnnotation<?>, OutputAnnotation<?>> implications,
        SetMultimap<OutputAnnotation<?>, OutputAnnotation<?>> overrides) {
        this.annotationMap = annotationMap;
        this.implications = implications;
        this.overrides = overrides;
        this.ordering = Ordering.explicit(Annotation.all()
            .asList())
            .onResultOf(Functions.forMap(annotationMap.inverse()));
    }

    public AnnotationSet activeAnnotations(Iterable<Annotation> annotations) {
        ImmutableList.Builder<OutputAnnotation<?>> writers = ImmutableList.builder();
        for (Annotation annotation : annotations) {
            OutputAnnotation<?> writer = annotationMap.get(annotation);
            if (writer != null) {
                writers.add(writer);
            }
        }
        return new AnnotationSet(writers.build());
    }
    
    public final class AnnotationSet {

        private ImmutableList<OutputAnnotation<?>> outputs;

        public AnnotationSet(ImmutableList<OutputAnnotation<?>> outputs) {
            this.outputs = outputs;
        }
        
        public <T> List<OutputAnnotation<? super T>> map(Class<? extends T> cls, Annotation constant) {
            Iterable<OutputAnnotation<? super T>> filtered = filter(outputs, cls, constant == null ? null : annotationMap.get(constant));
            List<OutputAnnotation<? super T>> flattened = flatten(filtered);
            return ordering.immutableSortedCopy(flattened);
        }

        private <T> List<OutputAnnotation<? super T>> flatten(Iterable<OutputAnnotation<? super T>> annotations) {
            Set<OutputAnnotation<? super T>> activeAnnotations = Sets.newHashSet();
            for (OutputAnnotation<? super T> annotation : annotations) {
                addWithImplied(activeAnnotations, annotation);
            }
            return ImmutableList.copyOf(activeAnnotations);
        }
        
        @SuppressWarnings("unchecked")
        private <T> void addWithImplied(Set<OutputAnnotation<? super T>> builder,
                                        OutputAnnotation<? super T> annotation) {
            for (OutputAnnotation<?> implied : implications.get(annotation)) {
                addWithImplied(builder,  (OutputAnnotation<? super T>)implied);
            }
            for (Object overridden : overrides.get(annotation)) {
                builder.remove(overridden);
            }
            builder.add(annotation);
        }
        
        private <T> Iterable<OutputAnnotation<? super T>> filter(Iterable<OutputAnnotation<?>> annotations,
            Class<? extends T> cls,
            OutputAnnotation<?> constant) {
            ImmutableSet.Builder<OutputAnnotation<? super T>> builder = ImmutableSet.builder();
            if (constant != null) {
                addIfApplies(builder, cls, constant);
            }
            Iterator<OutputAnnotation<?>> iter = annotations.iterator();
            while (iter.hasNext()) {
                addIfApplies(builder, cls, iter.next());
            }
            return builder.build();
        }
        
        @SuppressWarnings("unchecked")
        private <T> void addIfApplies(ImmutableSet.Builder<OutputAnnotation<? super T>> builder,
                                      Class<? extends T> cls,
                                      OutputAnnotation<?> writingAnnotation) {
            if (writingAnnotation.appliesTo(cls)) {
                builder.add((OutputAnnotation<? super T>) writingAnnotation);
            }
        }
        
    }
    

}
