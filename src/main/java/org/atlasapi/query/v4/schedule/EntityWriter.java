package org.atlasapi.query.v4.schedule;

import java.io.IOException;

public interface EntityWriter<T> {

    void write(T entity, FieldWriter writer, OutputContext ctxt) throws IOException;
    
    String fieldName();
    
}
