package org.atlasapi.query.v4.search;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.atlasapi.media.content.Content;
import org.atlasapi.output.AnnotationRegistry;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.query.v4.schedule.ContentListWriter;

import com.google.common.collect.FluentIterable;


public class ContentQueryResultWriter implements QueryResultWriter<Content> {

    private final AnnotationRegistry registry;
    
    public ContentQueryResultWriter(AnnotationRegistry annotations) {
        this.registry = checkNotNull(annotations);
    }

    @Override
    public void write(QueryResult<Content> result, ResponseWriter writer)
            throws IOException {
        writer.startResponse();
        writeResult(result, writer);
        writer.finishResponse();
    }

    private void writeResult(QueryResult<Content> result, ResponseWriter writer)
        throws IOException {

        OutputContext ctxt = outputContext(result.getContext());

        if (result.isListResult()) {
            FluentIterable<Content> resources = result.getResources();
            writer.writeList(new ContentListWriter(), resources, ctxt);
        } else {
            writer.writeObject(new ContentListWriter(), result.getOnlyResource(), ctxt);
        }
        
    }

    private OutputContext outputContext(QueryContext queryContext) {
        return new OutputContext(
            registry.activeAnnotations(queryContext.getAnnotations()),
            queryContext.getApplicationConfiguration());
    }
}
