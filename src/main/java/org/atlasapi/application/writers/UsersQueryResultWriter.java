package org.atlasapi.application.writers;

import java.io.IOException;

import org.atlasapi.application.users.User;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;

import com.google.common.collect.FluentIterable;


public class UsersQueryResultWriter implements QueryResultWriter<User> {
    private final EntityListWriter<User> usersListWriter;

    public UsersQueryResultWriter(EntityListWriter<User> usersListWriter) {
        this.usersListWriter = usersListWriter;
    }

    @Override
    public void write(QueryResult<User> result, ResponseWriter responseWriter) throws IOException {
        responseWriter.startResponse();
        writeResult(result, responseWriter);
        responseWriter.finishResponse();       
    }
    
    private void writeResult(QueryResult<User> result, ResponseWriter writer)
            throws IOException {
        OutputContext ctxt = outputContext(result.getContext());

        if (result.isListResult()) {
            FluentIterable<User> resources = result.getResources();
            writer.writeList(usersListWriter, resources, ctxt);
        } else {
            writer.writeObject(usersListWriter, result.getOnlyResource(), ctxt);
        }
    }

    private OutputContext outputContext(QueryContext queryContext) {
        return OutputContext.valueOf(queryContext);
    }

}
