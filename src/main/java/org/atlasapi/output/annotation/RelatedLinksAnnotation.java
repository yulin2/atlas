package org.atlasapi.output.annotation;


import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.output.writers.RelatedLinkWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class RelatedLinksAnnotation extends OutputAnnotation<Content> {

    public RelatedLinksAnnotation() {
        super(Content.class);
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeList(new RelatedLinkWriter(), entity.getRelatedLinks(), ctxt);
    }

}
