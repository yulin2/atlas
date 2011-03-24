package org.atlasapi.remotesite.pa;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.base.Maybe;

@Controller
public class PaSingleDateUpdatingController {
    
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("paSingleDateUpdater").build());
    private final PaProgDataProcessor processor;
    private final AdapterLog log;
    private final PaProgrammeDataStore fileManager;

    public PaSingleDateUpdatingController(PaProgDataProcessor processor, AdapterLog log, PaProgrammeDataStore fileManager) {
        this.processor = processor;
        this.log = log;
        this.fileManager = fileManager;
    }
    
    @PreDestroy
    public void shutDown() {
        executor.shutdown();
    }

    @RequestMapping("/system/update/pa/{dateString}")
    public void runUpdate(@PathVariable String dateString, HttpServletResponse response) {
        executor.execute(new PaSingleDateUpdater(processor, log, fileManager, dateString));
        response.setStatus(200);
    }
    
    @RequestMapping("/system/update/pa/{dateString}/{channelString}")
    public void runUpdate(@PathVariable String dateString, @PathVariable String channelString, HttpServletResponse response) {
        Maybe<Channel> channel = Channel.fromKey(channelString);
        if (channel.hasValue()) {
            PaSingleDateUpdater updater = new PaSingleDateUpdater(processor, log, fileManager, dateString);
            updater.supportChannels(ImmutableList.of(channel.requireValue()));
            executor.execute(updater);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
