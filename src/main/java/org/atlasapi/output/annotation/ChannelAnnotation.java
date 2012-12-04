package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.CHANNEL;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class ChannelAnnotation extends OutputAnnotation<Content> {

    public ChannelAnnotation(IdentificationAnnotation idAnnotation) {
        super(CHANNEL, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
