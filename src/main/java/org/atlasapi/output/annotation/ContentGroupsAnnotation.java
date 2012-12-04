package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.output.Annotation;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class ContentGroupsAnnotation extends OutputAnnotation<Content> {

    public ContentGroupsAnnotation(IdentificationAnnotation idAnnotation) {
        super(Annotation.CONTENT_GROUPS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
