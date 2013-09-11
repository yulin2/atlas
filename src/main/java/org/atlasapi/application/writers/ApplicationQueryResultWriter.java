package org.atlasapi.application.writers;

import java.io.IOException;

import org.atlasapi.application.model.Application;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;

import com.google.common.collect.FluentIterable;

public class ApplicationQueryResultWriter implements QueryResultWriter<Application> {

    private final EntityListWriter<Application> applicationListWriter;

    public ApplicationQueryResultWriter(EntityListWriter<Application> applicationListWriter) {
        this.applicationListWriter = applicationListWriter;
    }

    @Override
    public void write(QueryResult<Application> result, ResponseWriter responseWriter)
            throws IOException {
        responseWriter.startResponse();
        writeResult(result, responseWriter);
        responseWriter.finishResponse();
    }

    private void writeResult(QueryResult<Application> result, ResponseWriter writer)
            throws IOException {
        OutputContext ctxt = outputContext(result.getContext());

        if (result.isListResult()) {
            FluentIterable<Application> resources = result.getResources();
            writer.writeList(applicationListWriter, resources, ctxt);
        } else {
            writer.writeObject(applicationListWriter, result.getOnlyResource(), ctxt);
        }

    }

    private OutputContext outputContext(QueryContext queryContext) {
        return OutputContext.valueOf(queryContext);
    }
}
