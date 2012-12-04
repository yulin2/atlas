package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.PEOPLE;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class PeopleAnnotation extends OutputAnnotation<Content> {

    public PeopleAnnotation(IdentificationAnnotation idAnnotation) {
        super(PEOPLE, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
