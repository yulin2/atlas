package org.atlasapi.query.content.schedule;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.base.Maybe;

@Controller
public class ManualScheduleUpdateController {
    
    private final ScheduleWriter scheduleWriter;
    private final ContentResolver resolver;
    private ExecutorService executor;

    public ManualScheduleUpdateController(ScheduleWriter scheduleWriter, ContentResolver resolver) {
        this.scheduleWriter = scheduleWriter;
        this.resolver = resolver;
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Manual Schedule Update Thread").build());
    }
    
    @RequestMapping(value = "/system/schedule/update", method = RequestMethod.POST)
    public void runUpdate(HttpServletResponse response, @RequestParam(value = "uris", required = true) String uris) throws IOException {
        
        final ResolvedContent resolvedContent = resolver.findByCanonicalUris(Splitter.on(",").split(uris));
        
        executor.submit(new Runnable() {
            @Override
            public void run() {
                scheduleWriter.writeScheduleFor(Iterables.filter(resolvedContent.getAllResolvedResults(),Item.class));
            }
        });
        response.setStatus(200);
        
    }
    
}
