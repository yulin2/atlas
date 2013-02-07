package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AnnotationRegistry;
import org.atlasapi.output.annotation.OutputAnnotation;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.query.v4.schedule.EntityListWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;
import org.atlasapi.query.v4.schedule.QueryResultWriter;
import org.atlasapi.query.v4.schedule.ResponseWriter;

import com.google.common.collect.FluentIterable;

public class TopicQueryResultWriter implements QueryResultWriter<Topic> {

    private final class TopicWriter implements EntityListWriter<Topic> {

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
    
    private final AnnotationRegistry registry;
    
    public TopicQueryResultWriter(AnnotationRegistry annotations) {
        this.registry = checkNotNull(annotations);
    }

    @Override
    public void write(QueryResult<Topic> result, ResponseWriter writer) throws IOException {
        writer.startResponse();
        writeResult(result, writer);
        writer.finishResponse();
    }

    private void writeResult(QueryResult<Topic> result, ResponseWriter writer)
        throws IOException {

        OutputContext ctxt = outputContext(result.getContext());

        FluentIterable<Topic> topics = result.getResources();
        writer.writeList(new TopicWriter(), topics, ctxt);
    }
    
    private OutputContext outputContext(QueryContext queryContext) {
        return new OutputContext(
            registry.activeAnnotations(queryContext.getAnnotations()),
            queryContext.getApplicationConfiguration()
        );
    }
    
}
