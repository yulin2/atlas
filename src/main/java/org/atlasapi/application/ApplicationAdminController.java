package org.atlasapi.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.input.ModelReader;
import org.atlasapi.input.ReadException;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.query.common.Query;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParseException;
import org.atlasapi.query.common.QueryParser;
import org.atlasapi.query.common.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ApplicationAdminController {
    private static Logger log = LoggerFactory.getLogger(ApplicationAdminController.class);
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final QueryParser<Application> requestParser;
    private final QueryExecutor<Application> queryExecutor;
    private final QueryResultWriter<Application> resultWriter;
    private final ModelReader reader;
    private final ApplicationUpdater applicationUpdater;
    
    public ApplicationAdminController(QueryParser<Application> requestParser,
            QueryExecutor<Application> queryExecutor,
            QueryResultWriter<Application> resultWriter, 
            ModelReader reader,
            ApplicationUpdater applicationUpdater) {
        this.requestParser = requestParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
        this.reader = reader;
        this.applicationUpdater = applicationUpdater;
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
    public void outputAllApplications(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ResponseWriter writer = null;
        try {
           writer = writerResolver.writerFor(request, response);
           Query<Application> applicationsQuery = requestParser.parse(request);
           QueryResult<Application> queryResult = queryExecutor.execute(applicationsQuery);
           resultWriter.write(queryResult, writer);
        } catch (NotFoundException e) {
            sendError(request, response, writer, e, 404);
        } catch (Exception e) {
            sendError(request, response, writer, e, 500);
        }
    }
    
    @RequestMapping(value ="/4.0/applications", method = RequestMethod.POST)
    public void writeApplication(HttpServletRequest request, HttpServletResponse response) throws IOException, ReadException {
        Application application = deserialize(new InputStreamReader(request.getInputStream()));
        applicationUpdater.createOrUpdate(application);
    }
    
    @RequestMapping(value ="/4.0/applications/{aid}/sources", method = RequestMethod.POST)
    public void writeApplicationSources(HttpServletRequest request, HttpServletResponse response) throws IOException, ReadException, NotFoundException, QueryParseException {
        Query<Application> applicationsQuery = requestParser.parse(request);
        ApplicationSources sources = deserializeSources(new InputStreamReader(request.getInputStream()));
        applicationUpdater.updateSources(applicationsQuery.getOnlyId(), sources);
    }
    
    private Application deserialize(Reader input) throws IOException, ReadException {
        return reader.read(new BufferedReader(input), Application.class);
    }
    
    private ApplicationSources deserializeSources(Reader input) throws IOException, ReadException {
        return reader.read(new BufferedReader(input), ApplicationSources.class);
    }

}
