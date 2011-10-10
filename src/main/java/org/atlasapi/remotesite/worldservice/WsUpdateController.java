package org.atlasapi.remotesite.worldservice;

import static com.metabroadcast.common.http.HttpStatusCode.OK;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.remotesite.worldservice.WsProgrammeUpdate.WsProgrammeUpdateBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class WsUpdateController {

    private final WsProgrammeUpdateBuilder updateBuilder;
    private final ExecutorService executor;
    private final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZones.UTC);

    public WsUpdateController(WsProgrammeUpdateBuilder updateBuilder) {
        this.updateBuilder = updateBuilder;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    @RequestMapping(value="/system/update/worldservice", method=RequestMethod.POST)
    public void latestUpdate(HttpServletResponse response) {
        
        executor.submit(updateBuilder.updateLatest());
        
        response.setStatus(OK.code());
        
    }
    
    @RequestMapping(value="/system/update/worldservice/{date}", method=RequestMethod.POST)
    public void dateUpdate(HttpServletResponse response, @PathVariable(value="date") String date) {
        
        executor.submit(updateBuilder.updateForDate(dateFormat.parseDateTime(date)));
        
        response.setStatus(OK.code());
    }
    
}
