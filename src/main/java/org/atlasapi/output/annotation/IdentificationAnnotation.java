package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.IDENTIFICATION;

import java.io.IOException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;


public class IdentificationAnnotation extends OutputAnnotation<Identified> {
    
    public IdentificationAnnotation(IdentificationSummaryAnnotation identSummary) {
        super(IDENTIFICATION, Identified.class, ImmutableSet.of(identSummary));
    }

    @Override
    public void write(Identified entity, FieldWriter formatter) throws IOException {
        formatter.writeField("type", entity.getClass().getSimpleName().toLowerCase());
        formatter.writeField("uri", entity.getCanonicalUri());
    }

}
