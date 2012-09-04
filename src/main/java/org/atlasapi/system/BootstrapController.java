package org.atlasapi.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.persistence.bootstrap.ContentBootstrapper;
import org.atlasapi.persistence.bootstrap.elasticsearch.ESChangeListener;
import org.atlasapi.persistence.content.elasticsearch.ESContentIndexer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
public class BootstrapController {

    private static final Log log = LogFactory.getLog(BootstrapController.class);
    //
    private final ExecutorService scheduler = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ContentBootstrapper contentBootstrapper;
    private final ESContentIndexer esContentIndexer;

    public BootstrapController(ContentBootstrapper contentBootstrapper, ESContentIndexer esContentIndexer) {
        this.contentBootstrapper = contentBootstrapper;
        this.esContentIndexer = esContentIndexer;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/es")
    public void bootstrapES(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        int concurrencyLevel = 0;
        if (Strings.isNullOrEmpty(concurrency)) {
            concurrencyLevel = 1;
        } else {
            try {
                concurrencyLevel = Integer.parseInt(concurrency);
            } catch (NumberFormatException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad concurrency parameter!");
            }
        }
        try {
            final int actualConcurrency = concurrencyLevel;
            scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    boolean bootstrapping = contentBootstrapper.loadAllIntoListener(new ESChangeListener(esContentIndexer, actualConcurrency));
                    if (!bootstrapping) {
                        log.warn("Bootstrapping failed unexpectedly because apparently busy bootstrapping something else.");
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Currently bootstrapping another component.");
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/es/status")
    public void esBootstrapStatus(HttpServletResponse response) throws IOException {
        Map result = new HashMap();
        result.put("bootstrapping", contentBootstrapper.isBootstrapping());
        if (contentBootstrapper.isBootstrapping()) {
            result.put("destination", contentBootstrapper.getDestination());
        }
        jsonMapper.writeValue(response.getOutputStream(), result);
        response.flushBuffer();
    }
}
