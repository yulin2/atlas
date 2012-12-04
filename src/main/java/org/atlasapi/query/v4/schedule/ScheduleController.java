package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ScheduleController {

    private static Logger log = LoggerFactory.getLogger(ScheduleController.class);

    private static final Duration MAX_REQUEST_DURATION = Duration.standardDays(1);
    
    private final ScheduleRequestParser requestParser;
    private final ScheduleQueryExecutor queryExecutor;
    private final QueryResultWriter<ScheduleQueryResult> resultWriter;

    public ScheduleController(ScheduleQueryExecutor queryExecutor,
        ChannelResolver channelResolver,
        ApplicationConfigurationFetcher appFetcher,
        QueryResultWriter<ScheduleQueryResult> resultWriter) {
        this.requestParser = new ScheduleRequestParser(
            channelResolver,
            appFetcher,
            MAX_REQUEST_DURATION
        );
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
    }

    @RequestMapping({ "/4.0/schedules/{cid}.*", "/4.0/schedules/{cid}" })
    public void writeChannelSchedule(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        try {
            ScheduleQuery scheduleQuery = requestParser.queryFrom(request);
            ChannelSchedule channelSchedule = queryExecutor.execute(scheduleQuery);
            resultWriter.write(new ScheduleQueryResult(request,
                response,
                channelSchedule,
                scheduleQuery.getAnnotations(),
                scheduleQuery.getApplicationConfiguration()
            ));
        } catch (Exception e) {
            log.error("Request exception " + request.getRequestURI(), e);
            ErrorSummary exception = ErrorSummary.forException(e);
            resultWriter.writeError(new ErrorResult(request, response, exception));
        }
    }

}
