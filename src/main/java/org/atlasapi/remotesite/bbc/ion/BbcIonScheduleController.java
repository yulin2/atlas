package org.atlasapi.remotesite.bbc.ion;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.BbcIonScheduleClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class BbcIonScheduleController {

    private final DateTimeFormatter dateFormater = ISODateTimeFormat.basicDate().withZone(DateTimeZones.UTC);
    
    private final BbcIonScheduleHandler handler;
    private final AdapterLog log;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("singleBBCIonScheduleUpdater").build());

    private final BbcIonScheduleClient scheduleClient;


    public BbcIonScheduleController(BbcIonScheduleClient scheduleClient, BbcIonScheduleHandler handler, AdapterLog log) {
        this.scheduleClient = scheduleClient;
        this.handler = handler;
        this.log = log;
    }

    @RequestMapping("/system/bbc/ion/update/{service}/{date}")
    public void updateDay(@PathVariable String service, @PathVariable String date, HttpServletResponse response, @RequestParam(value="detail", required=false) String detail) throws IOException {
        
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
        
        executor.submit(new BbcIonScheduleUpdateTask(service, localDate, scheduleClient, handler, log));

        response.setStatus(HttpServletResponse.SC_OK);
    }
    
}
