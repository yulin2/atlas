package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.RELATED_LINKS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class RelatedLinksAnnotation extends OutputAnnotation<Content> {

    public RelatedLinksAnnotation(IdentificationAnnotation idAnnotation) {
        super(RELATED_LINKS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
