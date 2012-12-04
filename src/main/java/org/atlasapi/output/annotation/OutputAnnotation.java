package org.atlasapi.output.annotation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Objects;


public abstract class OutputAnnotation<T> {

    private final Class<T> appliesTo;

    public OutputAnnotation(Class<T> appliesTo) {
        this.appliesTo = checkNotNull(appliesTo);
    }

    public abstract void write(T entity, FieldWriter writer, OutputContext ctxt) throws IOException;
    
    public final Class<T> getAppliesTo() {
        return appliesTo;
    }
    
    public final boolean appliesTo(Class<?> other) {
        return appliesTo.isAssignableFrom(other);
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this).toString();
    }
}
