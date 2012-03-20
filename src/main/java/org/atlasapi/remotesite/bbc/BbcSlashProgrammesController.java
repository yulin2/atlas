package org.atlasapi.remotesite.bbc;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.content.Identified;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Controller
public class BbcSlashProgrammesController {

    private BbcProgrammeAdapter fetcher;
    private ExecutorService executor;

    public BbcSlashProgrammesController(BbcProgrammeAdapter programmeAdapter) {
        this.fetcher = programmeAdapter;
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("BBC /programmes %s").build());
    }

    @RequestMapping(value = "/system/update/bbc/programmes/{pid}", method = RequestMethod.POST)
    public void updatePid(HttpServletResponse response, HttpServletRequest request, @PathVariable("pid") final String pid) throws IOException {
        
        Future<Identified> result = executor.submit(new Callable<Identified>() {
            @Override
            public Identified call() {
                return fetcher.fetch(BbcFeeds.slashProgrammesUriForPid(pid));
            }
        });
        
        try {
            Identified ided = result.get();
            response.setStatus(200);
            response.setContentLength(ided.getCanonicalUri().length()+1);
            response.getWriter().println(ided.getCanonicalUri());
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace(response.getWriter());
        }
        
    }

}
