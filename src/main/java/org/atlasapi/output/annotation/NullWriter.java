package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class NullWriter extends OutputAnnotation<Object> {

    public NullWriter() {
        super(Object.class);
    }

    @Override
    public void write(Object entity, FieldWriter writer, OutputContext ctxt) throws IOException {

    }

}
