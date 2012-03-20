package org.atlasapi.remotesite.redux;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.media.content.Item;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.redux.ReduxDayUpdateTask.Builder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Controller
public class ReduxUpdateController {

    private final SiteSpecificAdapter<Item> adapter;

    private final Builder taskBuilder;
    private final DateTimeFormatter isoParser;
    private final ExecutorService executor;
    private final ContentWriter writer;
    
    public ReduxUpdateController(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
        this.writer = writer;
        this.adapter = adapter;
        this.taskBuilder = ReduxDayUpdateTask.dayUpdateTaskBuilder(client, writer, adapter, log);
        this.isoParser = DateTimeFormat.forPattern("yyyyMMdd");
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Redux Manual Updater %s").build());
    }
    
    @RequestMapping(value="/system/update/redux/day/{date}", method=RequestMethod.POST)
    public void updateDay(HttpServletResponse response, @PathVariable("date") String date){
        
        executor.submit(taskBuilder.updateFor(isoParser.parseDateTime(date).toLocalDate()));
        
    }
    
    @RequestMapping(value="/system/update/redux/diskref/{dr}", method=RequestMethod.POST)
    public void updateDiskref(HttpServletResponse response, @PathVariable("dr") final String diskRef) {
        
        executor.submit(new Runnable() {
            @Override
            public void run() {
                writer.createOrUpdate(adapter.fetch(FullProgrammeItemExtractor.REDUX_URI_BASE + "/programme/" + diskRef));
            }
        });
        
    }
    
}
