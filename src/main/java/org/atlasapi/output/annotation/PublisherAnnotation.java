package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class PublisherAnnotation extends OutputAnnotation<Content> {

    public PublisherAnnotation() {
        super(Content.class);
    }

    @Override
    public void write(Content entity, FieldWriter format, OutputContext ctxt) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
