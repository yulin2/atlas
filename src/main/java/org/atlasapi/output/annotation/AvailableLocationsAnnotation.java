package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.AVAILABLE_LOCATIONS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class AvailableLocationsAnnotation extends OutputAnnotation<Content> {

    public AvailableLocationsAnnotation(IdentificationAnnotation idAnnotation) {
        super(AVAILABLE_LOCATIONS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
