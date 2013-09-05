package org.atlasapi.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.NotAcceptableException;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.output.UnsupportedFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationAdminController {
    private static Logger log = LoggerFactory.getLogger(ApplicationAdminController.class);
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    
    public ApplicationAdminController() {
        
    }
    
    public void sendError(HttpServletRequest request, HttpServletResponse response, ResponseWriter writer, Exception e, int responseCode) throws IOException {
        response.setStatus(responseCode);
        log.error("Request exception " + request.getRequestURI(), e);
        ErrorSummary summary = ErrorSummary.forException(e);
        new ErrorResultWriter().write(summary, writer, request, response);
    }

}
