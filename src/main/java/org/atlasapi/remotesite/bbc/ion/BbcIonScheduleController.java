package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.lucene.util.NamedThreadFactory;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleUpdater.BbcIonScheduleUpdateTask;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Preconditions;

@Controller
public class BbcIonScheduleController {

    private final ContentResolver localFetcher;
    private final DefinitiveContentWriter writer;
    private final AdapterLog log;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("singleBBCIonScheduleUpdater"));

    public BbcIonScheduleController(ContentResolver localFetcher, DefinitiveContentWriter writer, AdapterLog log) {
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.log = log;
    }

    @RequestMapping("/system/bbc/ion/update/{service}/{date}")
    public void updateDay(@PathVariable String service, @PathVariable String date, HttpServletResponse response) throws JAXBException {
        Preconditions.checkArgument(date.length() == 8, "the date must be 8 digits");
        
        String scheduleUri = String.format(BbcIonScheduleUriSource.SCHEDULE_PATTERN, service, date);
        BbcIonScheduleUpdateTask updater = new BbcIonScheduleUpdateTask(scheduleUri, HttpClients.webserviceClient(), localFetcher, writer, log);
        executor.execute(updater);
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
}
