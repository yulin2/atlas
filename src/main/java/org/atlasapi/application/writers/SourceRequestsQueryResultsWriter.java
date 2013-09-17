package org.atlasapi.application.writers;

import java.io.IOException;

import org.atlasapi.application.SourceRequest;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;

import com.google.common.collect.FluentIterable;


public class SourceRequestsQueryResultsWriter implements QueryResultWriter<SourceRequest> {
    private final EntityListWriter<SourceRequest> sourcesRequestListWriter;
    
    public SourceRequestsQueryResultsWriter(EntityListWriter<SourceRequest> sourcesRequestListWriter) {
        this.sourcesRequestListWriter = sourcesRequestListWriter;
    }
    @Override
    public void write(QueryResult<SourceRequest> result, ResponseWriter responseWriter)
            throws IOException {
        responseWriter.startResponse();
        writeResult(result, responseWriter);
        responseWriter.finishResponse();
    }
    private void writeResult(QueryResult<SourceRequest> result, ResponseWriter writer)
            throws IOException {
        OutputContext ctxt = outputContext(result.getContext());

        if (result.isListResult()) {
            FluentIterable<SourceRequest> resources = result.getResources();
            writer.writeList(sourcesRequestListWriter, resources, ctxt);
        } else {
            writer.writeObject(sourcesRequestListWriter, result.getOnlyResource(), ctxt);
        }
    }
    
    private OutputContext outputContext(QueryContext queryContext) {
        return OutputContext.valueOf(queryContext);
    }
}
