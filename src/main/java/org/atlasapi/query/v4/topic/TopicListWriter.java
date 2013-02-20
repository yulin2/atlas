package org.atlasapi.query.v4.topic;

import java.io.IOException;
import java.util.List;

import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.annotation.OutputAnnotation;

public class TopicListWriter implements EntityListWriter<Topic> {

    @Override
    public void write(Topic entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        List<OutputAnnotation<? super Topic>> annotations = ctxt.getAnnotations(entity.getClass(), Annotation.ID);
        for (int i = 0; i < annotations.size(); i++) {
            annotations.get(i).write(entity, writer, ctxt);
        }
    }

    @Override
    public String listName() {
        return "topics";
    }

    @Override
    public String fieldName() {
        return "topic";
    }

}
