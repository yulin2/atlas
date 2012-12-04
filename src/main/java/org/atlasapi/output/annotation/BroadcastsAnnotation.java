package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.BROADCASTS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class BroadcastsAnnotation extends OutputAnnotation<Content> {

    public BroadcastsAnnotation(IdentificationAnnotation idAnnotation) {
        super(BROADCASTS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
