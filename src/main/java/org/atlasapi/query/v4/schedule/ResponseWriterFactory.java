package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ResponseWriterFactory {

    public ResponseWriter writerFor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //TODO 
        return new JsonResponseWriter(request, response);
    }

}
