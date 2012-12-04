package org.atlasapi.output.annotation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Set;

import org.atlasapi.output.Annotation;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

public abstract class OutputAnnotation<T> {
    
    private static final Function<OutputAnnotation<?>, Annotation> TO_KEY = new Function<OutputAnnotation<?>, Annotation>() {

        @Override
        public Annotation apply(OutputAnnotation<?> input) {
            return input.getKey();
        }
    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Function<OutputAnnotation<?>, Annotation> toKey() {
        return (Function) TO_KEY;
    }

    private static final Ordering<OutputAnnotation<?>> ANNOTATION_ORDERING = Ordering.explicit(Annotation.all()
        .asList())
        .onResultOf(OutputAnnotation.toKey());

    public static final Ordering<OutputAnnotation<?>> ordering() {
        return ANNOTATION_ORDERING;
    }
    
    private final Annotation key;
    private final Class<T> appliesTo;
    private final Set<OutputAnnotation<? super T>> implied;

    public OutputAnnotation(Annotation key, Class<T> appliesTo, Iterable<? extends OutputAnnotation<? super T>> implied) {
        this.key = checkNotNull(key);
        this.appliesTo = checkNotNull(appliesTo);
        this.implied = ImmutableSet.copyOf(implied);
    }
    
    public OutputAnnotation(Annotation key, Class<T> appliesTo) {
        this(key, appliesTo, ImmutableSet.<OutputAnnotation<? super T>>of());
    }

    public abstract void write(T entity, FieldWriter writer) throws IOException;
    
    public final Annotation getKey() {
        return key;
    }
    
    public final Class<T> getAppliesTo() {
        return appliesTo;
    }
    
    public final boolean appliesTo(Class<?> other) {
        return appliesTo.isAssignableFrom(other);
    }

    public final Set<OutputAnnotation<? super T>> getImpliedAnnotations() {
        return implied;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof OutputAnnotation) {
            OutputAnnotation<?> other = (OutputAnnotation<?>) that;
            return key.equals(other.key);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return key.hashCode();
    }
    
    @Override
    public String toString() {
        return key.toKey();
    }
    
}
