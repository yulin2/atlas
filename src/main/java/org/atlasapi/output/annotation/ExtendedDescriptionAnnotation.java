package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.EXTENDED_DESCRIPTION;

import java.io.IOException;

import org.atlasapi.media.entity.Described;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class ExtendedDescriptionAnnotation extends OutputAnnotation<Described> {

    public ExtendedDescriptionAnnotation(DescriptionAnnotation descAnnotation, ExtendedIdentificationAnnotation extIdent) {
        super(EXTENDED_DESCRIPTION, Described.class, ImmutableSet.<OutputAnnotation<? super Described>>of(descAnnotation, extIdent));
    }

    @Override
    public void write(Described entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
