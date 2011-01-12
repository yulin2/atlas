package org.atlasapi.remotesite.pa;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.util.NamedThreadFactory;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PaSingleDateUpdatingController {
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("paSingleDateUpdater"));
    private final PaProgrammeProcessor processor;
    private final AdapterLog log;
    private final PaLocalFileManager fileManager;

    public PaSingleDateUpdatingController(PaProgrammeProcessor processor, AdapterLog log, PaLocalFileManager fileManager) {
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
}
