package org.atlasapi.remotesite.bbc.schedule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Controller
public class BbcScheduleController {
    
    private final ContentResolver localFetcher;
    private final BbcProgrammeAdapter remoteFetcher;
    private final ContentWriter writer;
    private final AdapterLog log;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("singleBBCScheduleUpdater").build());

    public BbcScheduleController(ContentResolver localFetcher, BbcProgrammeAdapter remoteFetcher, ContentWriter writer, AdapterLog log) {
        this.localFetcher = localFetcher;
        this.remoteFetcher = remoteFetcher;
        this.writer = writer;
        this.log = log;
    }

    @RequestMapping("/system/bbc/update/{service}/{date}")
    public void updateDay(@PathVariable String service, @PathVariable String date, HttpServletResponse response) throws JAXBException {
        Preconditions.checkArgument(date.length() == 8, "the date must be 8 digits");
        String dateString = date.substring(0, 4)+"/"+date.substring(4, 6)+"/"+date.substring(6, 8);
        String baseScheduleUri = RadioPlayerServices.all.get(service).getScheduleUri();
        
        String scheduleUri = String.format("%s/%s.xml", baseScheduleUri, dateString);
        BbcScheduledProgrammeUpdater updater = new BbcScheduledProgrammeUpdater(localFetcher, remoteFetcher, writer, ImmutableSet.of(scheduleUri), log);
        executor.execute(updater);
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
