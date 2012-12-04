package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

public class IdentificationAnnotation extends OutputAnnotation<Identified> {
    
    public IdentificationAnnotation() {
        super(Identified.class);
    }

    @Override
    public void write(Identified entity, FieldWriter formatter, OutputContext ctxt) throws IOException {
        formatter.writeField("type", entity.getClass().getSimpleName().toLowerCase());
        formatter.writeField("uri", entity.getCanonicalUri());
    }

}
