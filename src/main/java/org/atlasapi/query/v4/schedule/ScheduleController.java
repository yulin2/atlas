package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableSet;

@Controller
public class ScheduleController {

    private static Logger log = LoggerFactory.getLogger(ScheduleController.class);

    private static final Duration MAX_REQUEST_DURATION = Duration.standardDays(1);
    
    private final ScheduleRequestParser requestParser;
    private final ScheduleQueryExecutor queryExecutor;
    private final AtlasModelWriter<ChannelSchedule> modelWriter;
    
    public ScheduleController(ScheduleQueryExecutor queryExecutor, ChannelResolver channelResolver, ApplicationConfigurationFetcher appFetcher, AtlasModelWriter<ChannelSchedule> modelWriter) {
        this.requestParser = new ScheduleRequestParser(channelResolver, appFetcher, MAX_REQUEST_DURATION);
        this.queryExecutor = queryExecutor;
        this.modelWriter = modelWriter;
    }
    
    @RequestMapping({"/4.0/schedules/{cid}.*", "/4.0/schedules/{cid}"})
    public void writeChannelSchedule(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ScheduleQuery scheduleQuery = requestParser.queryFrom(request);
            ChannelSchedule channelSchedule = queryExecutor.execute(scheduleQuery);
            modelWriter.writeTo(request, response, channelSchedule, scheduleQuery.getAnnotations(), scheduleQuery.getApplicationConfiguration());
        }catch(Exception e) {
            log.error("Request exception " + request.getRequestURI(), e);
            AtlasErrorSummary exception = AtlasErrorSummary.forException(e);
            modelWriter.writeError(request, response, exception);
        }
    }

}
