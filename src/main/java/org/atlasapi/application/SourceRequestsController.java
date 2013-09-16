package org.atlasapi.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.model.SourceRequest;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParseException;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.query.common.StandardQueryParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SourceRequestsController {
    private final StandardQueryParser<SourceRequest> queryParser;
    private final QueryExecutor<SourceRequest> queryExecutor;
    private final QueryResultWriter<SourceRequest> resultWriter;
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    
    
    public SourceRequestsController(StandardQueryParser<SourceRequest> queryParser,
            QueryExecutor<SourceRequest> queryExecutor,
            QueryResultWriter<SourceRequest> resultWriter) {
        this.queryParser = queryParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
    }
    
    @RequestMapping({"/4.0/sources/requests.*", "/4.0/sources/{sid}/requests.*"})
    public void listSourceRequests(HttpServletRequest request, 
            HttpServletResponse response) throws IOException, QueryParseException, QueryExecutionException {
        ResponseWriter writer = null;
        writer = writerResolver.writerFor(request, response);
        Query<SourceRequest> sourcesQuery = queryParser.parse(request);
        QueryResult<SourceRequest> queryResult = queryExecutor.execute(sourcesQuery);
        resultWriter.write(queryResult, writer);
    }
    
    
}
