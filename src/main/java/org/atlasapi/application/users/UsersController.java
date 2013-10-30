package org.atlasapi.application.users;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.auth.UserFetcher;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParser;
import org.atlasapi.query.common.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UsersController {
    private static Logger log = LoggerFactory.getLogger(UsersController.class);
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final QueryParser<User> requestParser;
    private final QueryExecutor<User> queryExecutor;
    private final QueryResultWriter<User> resultWriter;
    private final UserFetcher userFetcher;
    
    public UsersController(QueryParser<User> requestParser,
            QueryExecutor<User> queryExecutor, 
            QueryResultWriter<User> resultWriter,
            UserFetcher userFetcher) {
        this.requestParser = requestParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
        this.userFetcher = userFetcher;
    }

    @RequestMapping({ "/4.0/users/{uid}.*", "/4.0/users.*" })
    public void outputUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseWriter writer = null;
        try {
            writer = writerResolver.writerFor(request, response);
            Query<User> applicationsQuery = requestParser.parse(request);
            QueryResult<User> queryResult = queryExecutor.execute(applicationsQuery);
            resultWriter.write(queryResult, writer);
        } catch (Exception e) {
            log.error("Request exception " + request.getRequestURI(), e);
            ErrorSummary summary = ErrorSummary.forException(e);
            new ErrorResultWriter().write(summary, writer, request, response);
        }
    }

}
