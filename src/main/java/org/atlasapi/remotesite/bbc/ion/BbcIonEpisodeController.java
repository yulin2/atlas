package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Controller
public class BbcIonEpisodeController {

    private final ContentWriter writer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("singleBBCIonEpisodeUpdater").build());
    private final BbcItemFetcherClient fetcherClient;

    public BbcIonEpisodeController(ContentWriter writer, AdapterLog log) {
        this.writer = writer;
        this.fetcherClient = new BbcIonEpisodeDetailItemFetcherClient(log);
    }

    @RequestMapping("/system/bbc/ion/update")
    public void updateDay(@RequestParam String pid, HttpServletResponse response, @RequestParam(value="detail", required=false) String detail) {
        
        executor.execute(new UpdateEpisode(pid));
        
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    class UpdateEpisode implements Runnable {
        
        private final String pid;

        public UpdateEpisode(String pid) {
            this.pid = pid;
        }

        @Override
        public void run() {
            Item item = fetcherClient.createItem(pid);
            if (item != null) {
                writer.createOrUpdate(item);
            }
        }
    }
}
