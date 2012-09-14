package org.atlasapi.remotesite.bbc;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Controller
public class BbcSlashProgrammesController {

    private SiteSpecificAdapter<Content> fetcher;
    private ExecutorService executor;

    public BbcSlashProgrammesController(SiteSpecificAdapter<Content> programmeAdapter) {
        this.fetcher = programmeAdapter;
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("BBC /programmes %s").build());
    }

    @RequestMapping(value = "/system/update/bbc/programmes/{pid}", method = RequestMethod.POST)
    public void updatePid(HttpServletResponse response, HttpServletRequest request, @PathVariable("pid") final String pid) throws IOException {
        
        Future<Content> result = executor.submit(new Callable<Content>() {
            @Override
            public Content call() {
                return fetcher.fetch(BbcFeeds.slashProgrammesUriForPid(pid));
            }
        });
        
        try {
            Identified ided = result.get();
            response.setStatus(200);
            String msg = ided == null ? "No content for " + pid
                                      : ided.getCanonicalUri();
            response.setContentLength(msg.length()+1);
            response.getWriter().println(msg);
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace(response.getWriter());
        }
        
    }

}
