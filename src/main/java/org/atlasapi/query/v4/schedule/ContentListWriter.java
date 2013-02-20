package org.atlasapi.query.v4.schedule;

import java.io.IOException;
import java.util.List;

import org.atlasapi.media.content.Content;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.annotation.OutputAnnotation;

public final class ContentListWriter implements EntityListWriter<Content> {

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        List<OutputAnnotation<? super Content>> annotations = ctxt.getAnnotations(entity.getClass(), Annotation.ID);
        for (int i = 0; i < annotations.size(); i++) {
            annotations.get(i).write(entity, writer, ctxt);
        }
    }

    @Override
    public String listName() {
        return "content";
    }

    @Override
    public String fieldName() {
        return "item";//TODO: get name from content for xml 
    }
}