package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Controller
public class BbcIonScheduleController {

    private final ContentResolver localFetcher;
    private final ContentWriter writer;
    private final AdapterLog log;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("singleBBCIonScheduleUpdater").build());
    private final BbcItemFetcherClient fetcherClient;
    private final BbcIonDeserializer<IonSchedule> deserialiser;

    public BbcIonScheduleController(ContentResolver localFetcher, ContentWriter writer, AdapterLog log) {
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.log = log;
        this.deserialiser = deserializerForClass(IonSchedule.class);
        this.fetcherClient = new BbcIonEpisodeDetailItemFetcherClient(log);
    }

    @RequestMapping("/system/bbc/ion/update/{service}/{date}")
    public void updateDay(@PathVariable String service, @PathVariable String date, HttpServletResponse response, @RequestParam(value="detail", required=false) String detail) {
        Preconditions.checkArgument(date.length() == 8, "the date must be 8 digits");
        
        String scheduleUri = String.format(BbcIonScheduleUriSource.SCHEDULE_PATTERN, service, date);
        BbcIonScheduleUpdater updater = new BbcIonScheduleUpdater(Executors.newSingleThreadExecutor(), ImmutableList.of(scheduleUri), localFetcher, writer, deserialiser, log);
        if(!Strings.isNullOrEmpty(detail) && Boolean.parseBoolean(detail)) {
            updater.withItemFetchClient(fetcherClient);
        }
        executor.execute(updater);
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
}
