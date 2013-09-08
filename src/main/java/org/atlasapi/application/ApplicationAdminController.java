package org.atlasapi.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.model.Application;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.query.common.Query;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParser;
import org.atlasapi.query.common.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ApplicationAdminController {
    private static Logger log = LoggerFactory.getLogger(ApplicationAdminController.class);
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final QueryParser<Application> requestParser;
    private final QueryExecutor<Application> queryExecutor;
    private final QueryResultWriter<Application> resultWriter;
    
    public ApplicationAdminController(QueryParser<Application> requestParser,
            QueryExecutor<Application> queryExecutor,
            QueryResultWriter<Application> resultWriter) {
        this.requestParser = requestParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
    }
    
    public void sendError(HttpServletRequest request, 
            HttpServletResponse response, 
            ResponseWriter writer, 
            Exception e, 
            int responseCode) throws IOException {
        response.setStatus(responseCode);
        log.error("Request exception " + request.getRequestURI(), e);
        ErrorSummary summary = ErrorSummary.forException(e);
        new ErrorResultWriter().write(summary, writer, request, response);
    }
    
    @RequestMapping({ "/4.0/applications/{aid}.*", "/4.0/applications.*" })
    public void writeAllApplications(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ResponseWriter writer = null;
        try {
           writer = writerResolver.writerFor(request, response);
           Query<Application> applicationsQuery = requestParser.parse(request);
           QueryResult<Application> queryResult = queryExecutor.execute(applicationsQuery);
           resultWriter.write(queryResult, writer);
        } catch (Exception e) {
            sendError(request, response, writer, e, 500);
        }
    }

}
