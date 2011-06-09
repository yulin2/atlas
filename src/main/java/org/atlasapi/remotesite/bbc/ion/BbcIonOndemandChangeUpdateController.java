package org.atlasapi.remotesite.bbc.ion;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class BbcIonOndemandChangeUpdateController {

    private final ExecutorService executor;
    private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZones.UTC);
    private final BbcIonOndemandChangeUpdateBuilder updateBuilder;

    public BbcIonOndemandChangeUpdateController(BbcIonOndemandChangeUpdateBuilder updateBuilder) {
        this.updateBuilder = updateBuilder;
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Manual Ondemand Change Updater %s").build());
    }

    @RequestMapping("/system/bbc/ion/update/ondemand/{datetime}")
    public void updateDay(@PathVariable("datetime") String datetime, HttpServletResponse response) throws IOException {
        
        final DateTime start = dateTimeFormatter.parseDateTime(datetime);
        
        executor.submit(new Runnable() {
            @Override
            public void run() {
                updateBuilder.updateStartingFrom(start).runUpdate();
            }
        });

        response.setStatus(200);
        response.getWriter().write(String.format("Updating ondemands from %s \n", start.toString("yyyy-MM-dd HH:mm:ss ZZ")));
    }
}
