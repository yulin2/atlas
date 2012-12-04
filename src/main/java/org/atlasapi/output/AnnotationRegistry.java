package org.atlasapi.output;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import org.atlasapi.output.annotation.OutputAnnotation;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;

public class AnnotationRegistry {

    private final EnumMap<Annotation, OutputAnnotation<?>> annotationMap;
    private final Function<Annotation, OutputAnnotation<?>> annotationTranslation;

    public AnnotationRegistry(Iterable<OutputAnnotation<?>> annotations) {
        this.annotationMap = Maps.newEnumMap(Annotation.class);
        annotationMap.putAll(Maps.uniqueIndex(annotations, OutputAnnotation.toKey()));
        this.annotationTranslation = Functions.forMap(annotationMap);
    }
    
    public <T> List<OutputAnnotation<? super T>> map(Iterable<Annotation> annotations, Class<? extends T> cls) {
        return map(annotations, cls, null);
    }
    
    public <T> List<OutputAnnotation<? super T>> map(Iterable<Annotation> annotations, Class<? extends T> cls, Annotation constant) {
        Iterable<OutputAnnotation<?>> output = Iterables.transform(annotations, annotationTranslation);
        Iterable<OutputAnnotation<? super T>> filtered = filter(output, cls, annotationTranslation.apply(constant));
        List<OutputAnnotation<? super T>> flattened = flatten(filtered);
        return OutputAnnotation.ordering().immutableSortedCopy(flattened);
    }
    
    private <T> List<OutputAnnotation<? super T>> flatten(Iterable<OutputAnnotation<? super T>> annotations) {
        ImmutableSet.Builder<OutputAnnotation<? super T>> builder = ImmutableSet.builder();
        for (OutputAnnotation<? super T> annotation : annotations) {
            addWithImplied(builder, annotation);
        }
        return builder.build().asList();
    }
    
    private <T> void addWithImplied(ImmutableSet.Builder<OutputAnnotation<? super T>> builder,
                                    OutputAnnotation<? super T> annotation) {
        for (OutputAnnotation<? super T> implied : annotation.getImpliedAnnotations()) {
            addWithImplied(builder, implied);
        }
        builder.add(annotation);
    }

    private <T> Iterable<OutputAnnotation<? super T>> filter(Iterable<OutputAnnotation<?>> annotations,
                                                             Class<? extends T> cls,
                                                             OutputAnnotation<?> constant) {
        Builder<OutputAnnotation<? super T>> builder = ImmutableSet.builder();
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
    private <T> void addIfApplies(Builder<OutputAnnotation<? super T>> builder,
                                  Class<? extends T> cls,
                                  OutputAnnotation<?> writingAnnotation) {
        if (writingAnnotation.appliesTo(cls)) {
            builder.add((OutputAnnotation<? super T>) writingAnnotation);
        }
    }

}
