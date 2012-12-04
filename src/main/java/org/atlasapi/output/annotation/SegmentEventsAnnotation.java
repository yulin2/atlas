package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.SEGMENT_EVENTS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class SegmentEventsAnnotation extends OutputAnnotation<Content> {

    public SegmentEventsAnnotation(IdentificationAnnotation idAnnotation) {
        super(SEGMENT_EVENTS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
