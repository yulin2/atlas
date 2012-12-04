package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.output.writers.ChildRefWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class SubItemAnnotation extends OutputAnnotation<Content> {

    private final ChildRefWriter childRefWriter;

    public SubItemAnnotation() {
        super(Content.class);
        childRefWriter = new ChildRefWriter("content");
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (entity instanceof Container) {
            Container container = (Container) entity;
            writer.writeList(childRefWriter, container.getChildRefs(), ctxt);
        }
    }

}
