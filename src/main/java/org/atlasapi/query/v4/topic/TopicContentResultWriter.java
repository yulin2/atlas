package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.AnnotationRegistry;
import org.atlasapi.output.ContextualResultWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.query.common.ContextualQueryResult;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.v4.schedule.ContentListWriter;

public class TopicContentResultWriter implements ContextualResultWriter<Topic, Content> {

    private final AnnotationRegistry registry;

    public TopicContentResultWriter(AnnotationRegistry annotations) {
        this.registry = checkNotNull(annotations);
    }

    @Override
    public void write(ContextualQueryResult<Topic, Content> result, ResponseWriter writer)
        throws IOException {
        writer.startResponse();
        writeResult(result, writer);
        writer.finishResponse();
    }

    private void writeResult(ContextualQueryResult<Topic, Content> result, ResponseWriter writer)
        throws IOException {

        OutputContext ctxt = outputContext(result.getContext());

        writer.writeObject(new TopicListWriter(), result.getContextResult().getOnlyResource(), ctxt);
        writer.writeList(new ContentListWriter(), result.getResourceResult().getResources(), ctxt);
        
    }

    private OutputContext outputContext(QueryContext queryContext) {
        return new OutputContext(
            registry.activeAnnotations(queryContext.getAnnotations()),
            queryContext.getApplicationConfiguration());
    }

}
