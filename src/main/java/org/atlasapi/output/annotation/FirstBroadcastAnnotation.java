package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.FIRST_BROADCASTS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class FirstBroadcastAnnotation extends OutputAnnotation<Content> {

    public FirstBroadcastAnnotation(IdentificationAnnotation idAnnotation) {
        super(FIRST_BROADCASTS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}

