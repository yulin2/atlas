package org.atlasapi.query.v4.topic;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.query.v4.schedule.ErrorResultWriter;
import org.atlasapi.query.v4.schedule.QueryResultWriter;
import org.atlasapi.query.v4.schedule.ResponseWriter;
import org.atlasapi.query.v4.schedule.ResponseWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

public class TopicController {

    private static Logger log = LoggerFactory.getLogger(TopicController.class);

    private final QueryParser<TopicQuery> requestParser;
    private final TopicQueryExecutor queryExecutor;
    private final QueryResultWriter<TopicQueryResult> resultWriter;

    private ResponseWriterFactory writerResolver = new ResponseWriterFactory();

    public TopicController(TopicQueryExecutor queryExecutor,
        ApplicationConfigurationFetcher appFetcher,
        QueryResultWriter<TopicQueryResult> resultWriter, TopicQueryParser topicQueryParser) {
        this.requestParser = topicQueryParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
    }

    @RequestMapping({ "/4.0/topics/{tid}.*", "/4.0/topics/{tid}" })
    public void writeTopic(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ResponseWriter writer = null;
        try {
            writer = writerResolver.writerFor(request, response);
            TopicQuery topicQuery = requestParser.queryFrom(request);
            TopicQueryResult queryResult = queryExecutor.execute(topicQuery);
            resultWriter.write(queryResult, writer);
        } catch (Exception e) {
            log.error("Request exception " + request.getRequestURI(), e);
            ErrorSummary summary = ErrorSummary.forException(e);
            new ErrorResultWriter().write(summary, writer, request, response);
        }
    }

}
