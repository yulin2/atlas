package org.atlasapi.query.v4.topic;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParser;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.query.v4.schedule.ErrorResultWriter;
import org.atlasapi.query.v4.schedule.QueryResultWriter;
import org.atlasapi.query.v4.schedule.ResponseWriter;
import org.atlasapi.query.v4.schedule.ResponseWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

public class TopicController {

    private static Logger log = LoggerFactory.getLogger(TopicController.class);

    private final QueryParser<Topic> requestParser;
    private final QueryExecutor<Topic> queryExecutor;
    private final QueryResultWriter<Topic> resultWriter;

    private ResponseWriterFactory writerResolver = new ResponseWriterFactory();

    public TopicController(QueryExecutor<Topic> queryExecutor,
        ApplicationConfigurationFetcher appFetcher,
        QueryResultWriter<Topic> resultWriter, TopicQueryParser topicQueryParser) {
        this.requestParser = topicQueryParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
    }

    @RequestMapping({ "/4.0/topics/{tid}.*", "/4.0/topics/{tid}", "/4.0/topics.*", "/4.0/topics" })
    public void writeSingleTopic(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ResponseWriter writer = null;
        try {
            writer = writerResolver.writerFor(request, response);
            Query<Topic> topicQuery = requestParser.parse(request);
            QueryResult<Topic> queryResult = queryExecutor.execute(topicQuery);
            resultWriter.write(queryResult, writer);
        } catch (Exception e) {
            log.error("Request exception " + request.getRequestURI(), e);
            ErrorSummary summary = ErrorSummary.forException(e);
            new ErrorResultWriter().write(summary, writer, request, response);
        }
    }

}
