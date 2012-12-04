package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.FILTERING_RESOURCE;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class FilteringResourceAnnotation extends OutputAnnotation<Content> {

    public FilteringResourceAnnotation(IdentificationAnnotation idAnnotation) {
        super(FILTERING_RESOURCE, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub

    }

}
