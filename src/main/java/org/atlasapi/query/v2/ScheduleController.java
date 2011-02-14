package org.atlasapi.query.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.metabroadcast.common.webapp.query.DateTimeInQueryParser;

@Controller
public class ScheduleController extends BaseController {
    
    private final DateTimeInQueryParser dateTimeInQueryParser = new DateTimeInQueryParser();
    
    public ScheduleController(KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter outputter) {
        super(executor, configFetcher, log, outputter);
    }

    @RequestMapping("/3.0/schedule.*")
    public void schedule(@RequestParam(required=false) String to, @RequestParam(required=false) String from, @RequestParam(required=false) String on, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ContentQuery filter = builder.build(request);

            if (! Strings.isNullOrEmpty(on)) {
                ContentQuery.joinTo(filter, ContentQueryBuilder.query().equalTo(Attributes.BROADCAST_TRANSMISSION_TIME, dateTimeInQueryParser.parse(on)).build());
            } else if (! Strings.isNullOrEmpty(to) && ! Strings.isNullOrEmpty(from)) {
                ContentQuery.joinTo(filter, ContentQueryBuilder.query()
                        .after(Attributes.BROADCAST_TRANSMISSION_END_TIME, dateTimeInQueryParser.parse(from))
                        .before(Attributes.BROADCAST_TRANSMISSION_TIME, dateTimeInQueryParser.parse(to)).build());
            } else {
                throw new IllegalArgumentException("You must pass either 'on' or 'from' and 'to'");
            }
            
            Schedule schedule = executor.schedule(filter);
            modelAndViewFor(request, response, schedule.toScheduleChannels());
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
    
}
