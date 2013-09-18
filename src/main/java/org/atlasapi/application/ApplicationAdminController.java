package org.atlasapi.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.input.ModelReader;
import org.atlasapi.input.ReadException;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.query.common.Query;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.persistence.application.ApplicationStore;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParseException;
import org.atlasapi.query.common.QueryParser;
import org.atlasapi.query.common.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

@Controller
public class ApplicationAdminController {

    private static Logger log = LoggerFactory.getLogger(ApplicationAdminController.class);
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final QueryParser<Application> requestParser;
    private final QueryExecutor<Application> queryExecutor;
    private final QueryResultWriter<Application> resultWriter;
    private final ModelReader reader;
    private final NumberToShortStringCodec idCodec;
    private final SourceIdCodec sourceIdCodec;
    private final ApplicationStore applicationStore;

    public ApplicationAdminController(QueryParser<Application> requestParser,
            QueryExecutor<Application> queryExecutor,
            QueryResultWriter<Application> resultWriter,
            ModelReader reader,
            NumberToShortStringCodec idCodec,
            SourceIdCodec sourceIdCodec,
            ApplicationStore applicationStore) {
        this.requestParser = requestParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
        this.reader = reader;
        this.idCodec = idCodec;
        this.sourceIdCodec = sourceIdCodec;
        this.applicationStore = applicationStore;
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

    @RequestMapping(value = "/4.0/applications", method = RequestMethod.POST)
    public void writeApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ReadException, NotFoundException {
        Application application = deserialize(new InputStreamReader(request.getInputStream()), Application.class);
        if (application.getId() != null) {
            Optional<Application> existing = applicationStore.applicationFor(application.getId());
            application = application.copy().withSlug(existing.get().getSlug()).build();
            applicationStore.updateApplication(application);
        } else {
            // New application
            applicationStore.createApplication(application);
        }
        
    }

    @RequestMapping(value = "/4.0/applications/{aid}/sources", method = RequestMethod.POST)
    public void writeApplicationSources(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String aid)
            throws IOException, ReadException, NotFoundException, QueryParseException {
        Id applicationId = Id.valueOf(idCodec.decode(aid));
        ApplicationSources sources = deserialize(new InputStreamReader(
                request.getInputStream()), ApplicationSources.class);
        Application existing = applicationStore.applicationFor(applicationId).get();
        Application modified = existing.replaceSources(sources);
        applicationStore.updateApplication(modified);
    }
    
    @RequestMapping(value = "/4.0/applications/{aid}/precedence", method = RequestMethod.POST)
    public void setPrecedenceOrder(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String aid) throws NotFoundException, IOException, ReadException, QueryExecutionException  {
        Id applicationId = Id.valueOf(idCodec.decode(aid));
        PrecedenceOrdering ordering = deserialize(new InputStreamReader(request.getInputStream()), PrecedenceOrdering.class);
        List<Publisher> sourceOrder = getSourcesFrom(ordering);
        Application existing = applicationStore.applicationFor(applicationId).get();
        applicationStore.updateApplication(existing.setPrecendenceOrder(sourceOrder));
    }
    
    @RequestMapping(value = "/4.0/applications/{aid}/precedence", method = RequestMethod.DELETE)
    public void disablePrecedence(HttpServletRequest request, HttpServletResponse response) throws QueryParseException, NotFoundException {
        Query<Application> applicationsQuery = requestParser.parse(request);
        Application existing = applicationStore.applicationFor(applicationsQuery.getOnlyId()).get();
        applicationStore.updateApplication(existing.disablePrecendence());
    }

    private <T> T deserialize(Reader input, Class<T> cls) throws IOException, ReadException {
        return reader.read(new BufferedReader(input), cls);
    }
    
    private List<Publisher> getSourcesFrom(PrecedenceOrdering ordering) throws QueryExecutionException {
        ImmutableList.Builder<Publisher> sources =ImmutableList.builder();
        for (String sourceId : ordering.getOrdering()) {
            Optional<Publisher> source = sourceIdCodec.decode(sourceId);
            if (source.isPresent()) {
                sources.add(source.get());
            } else {
                throw new QueryExecutionException("No publisher by id " + sourceId);
            }
        }
        return sources.build();
    }
}
