package org.atlasapi.query.v4.schedule;

import java.io.IOException;

public interface QueryResultWriter<T> {

    void write(T result, ResponseWriter responseWriter) throws IOException;

}