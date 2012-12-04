package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ResponseWriter extends FieldWriter {

    void startResponse(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void finishResponse(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
