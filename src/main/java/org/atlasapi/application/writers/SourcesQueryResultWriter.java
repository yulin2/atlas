package org.atlasapi.application.writers;

import java.io.IOException;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;

import com.google.common.collect.FluentIterable;


public class SourcesQueryResultWriter implements QueryResultWriter<Publisher> {
    private final EntityListWriter<Publisher> sourcesWriter;
    
    public SourcesQueryResultWriter(EntityListWriter<Publisher> sourcesWriter) {
        this.sourcesWriter = sourcesWriter;
    }

    @Override
    public void write(QueryResult<Publisher> result, ResponseWriter responseWriter)
            throws IOException {
        responseWriter.startResponse();
        writeResult(result, responseWriter);
        responseWriter.finishResponse();
    }
    
    private void writeResult(QueryResult<Publisher> result, ResponseWriter writer)
            throws IOException {
        OutputContext ctxt = outputContext(result.getContext());

        if (result.isListResult()) {
            FluentIterable<Publisher> resources = result.getResources();
            writer.writeList(sourcesWriter, resources, ctxt);
        } else {
            writer.writeObject(sourcesWriter, result.getOnlyResource(), ctxt);
        }

    }
    
    private OutputContext outputContext(QueryContext queryContext) {
        return OutputContext.valueOf(queryContext);
    }

}
