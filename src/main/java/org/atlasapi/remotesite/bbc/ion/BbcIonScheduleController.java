package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.remotesite.bbc.BbcModule.SCHEDULE_DEFAULT_FORMAT;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class BbcIonScheduleController {

    private final DateTimeFormatter dateFormater = ISODateTimeFormat.basicDate().withZone(DateTimeZones.UTC);
    
    private final BbcIonBroadcastHandler handler;
    private final AdapterLog log;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("singleBBCIonScheduleUpdater").build());

    private final RemoteSiteClient<IonSchedule> scheduleClient;
    private final BroadcastTrimmer scheduleTrimmer;
    private final ChannelResolver channelResolver;

    public BbcIonScheduleController(RemoteSiteClient<IonSchedule> scheduleClient, BbcIonBroadcastHandler handler, BroadcastTrimmer schedulerTrimmer, ChannelResolver channelResolver, AdapterLog log) {
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.scheduleTrimmer = schedulerTrimmer;
        this.channelResolver = channelResolver;
        this.log = log;
    }

    @RequestMapping("/system/bbc/ion/update/{service}/{date}")
    public void updateDay(@PathVariable String service, @PathVariable String date, HttpServletResponse response) throws IOException {
        
        if(!BbcIonServices.services.keySet().contains(service)) {
            response.sendError(400, "Unknown service " + service);
            return;
        }
        
        DateTime localDate;
        try {
            localDate = dateFormater.parseDateTime(date);
        }catch (Exception e) {
            response.sendError(400, "Invalid date format. Expects yyyyMMdd");
            return;
        }
        
        executor.submit(new BbcIonScheduleUpdateTask(String.format(SCHEDULE_DEFAULT_FORMAT, service, localDate), scheduleClient, handler, scheduleTrimmer, channelResolver, log));

        response.setStatus(HttpServletResponse.SC_OK);
    }
    
}
