package org.atlasapi.remotesite.itv.whatson;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.http.HttpStatusCode;

@Controller
public class ItvWhatsOnController {
    private final DateTimeFormatter parser = ISODateTimeFormat.date();
    private final String feedUrl;
    private final RemoteSiteClient<List<ItvWhatsOnEntry>> itvWhatsOnClient;
    private final ItvWhatsOnEntryProcessor processor;
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public ItvWhatsOnController(String feedUrl, 
            RemoteSiteClient<List<ItvWhatsOnEntry>> itvWhatsOnClient, 
            ItvWhatsOnEntryProcessor processor) {
        this.feedUrl = feedUrl;
        this.itvWhatsOnClient = itvWhatsOnClient;
        this.processor = processor;
    }

    /**
     * Manually update a day. Needed when rerunning a past ingest to bring in new data.
     */
    @RequestMapping(value="/system/update/itv/whatson/{day}",method=RequestMethod.POST)
    public void updateDay(@PathVariable String day, HttpServletResponse response) {
        LocalDate scheduleDay = parser.parseLocalDate(day);
        log.info("Rerunning ITV What's On Ingest for " + day.toString());
        ItvWhatsOnUpdater updater = ItvWhatsOnUpdater.builder()
                .withFeedUrl(feedUrl)
                .withWhatsOnClient(itvWhatsOnClient)
                .withProcessor(processor)
                .withPercentageFailureToTriggerJobFailure(0)
                .withDay(scheduleDay)
                .build();
        updater.run();  
        response.setStatus(HttpStatusCode.OK.code());
        response.setContentLength(0);
    }
}
