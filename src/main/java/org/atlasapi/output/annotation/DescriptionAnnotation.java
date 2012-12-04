package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.DESCRIPTION;

import org.atlasapi.media.entity.Described;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class DescriptionAnnotation extends OutputAnnotation<Described> {

    public DescriptionAnnotation(IdentificationAnnotation idAnnotation) {
        super(DESCRIPTION, Described.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Described content, FieldWriter writer) {

    }

}
