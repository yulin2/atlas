package org.atlasapi.remotesite.bbc.ion;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.joda.time.LocalDate;
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
    private final ContentResolver localFetcher;
    private final ContentWriter writer;
    private final AdapterLog log;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("singleBBCIonScheduleUpdater").build());
    private final BbcItemFetcherClient fetcherClient;
    private final ItemsPeopleWriter itemsPeopleWriter;

    public BbcIonScheduleController(ContentResolver localFetcher, ContentWriter writer, ItemsPeopleWriter itemsPeopleWriter, AdapterLog log) {
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.itemsPeopleWriter = itemsPeopleWriter;
        this.log = log;
        this.fetcherClient = new BbcIonEpisodeDetailItemFetcherClient(log);
    }

    @RequestMapping("/system/bbc/ion/update/{service}/{date}")
    public void updateDay(@PathVariable String service, @PathVariable String date, HttpServletResponse response, @RequestParam(value="detail", required=false) String detail) throws IOException {
        
        if(!BbcIonServices.services.keySet().contains(service)) {
            response.sendError(400, "Unknown service " + service);
            return;
        }
        
        try {
            LocalDate localDate = dateFormater.parseDateTime(date).toLocalDate();
            executor.execute(new BbcIonScheduleUpdateTask(service, localDate, HttpClients.webserviceClient(), localFetcher, writer, log)
                .withItemFetcherClient(fetcherClient)
                .withItemPeopleWriter(itemsPeopleWriter)
            );
        }catch (Exception e) {
            response.sendError(400, "Invalid date format. Expects yyyyMMdd");
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
}
