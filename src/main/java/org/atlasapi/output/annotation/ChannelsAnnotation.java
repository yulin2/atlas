package org.atlasapi.output.annotation;

import static org.atlasapi.output.Annotation.CHANNELS;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;

import com.google.common.collect.ImmutableSet;

public class ChannelsAnnotation extends OutputAnnotation<Content> {

    public ChannelsAnnotation(IdentificationAnnotation idAnnotation) {
        super(CHANNELS, Content.class, ImmutableSet.of(idAnnotation));
    }

    @Override
    public void write(Content entity, FieldWriter format) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
