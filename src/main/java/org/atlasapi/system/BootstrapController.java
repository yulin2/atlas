package org.atlasapi.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.persistence.bootstrap.ChangeListener;
import org.atlasapi.persistence.bootstrap.ContentBootstrapper;
import org.atlasapi.persistence.bootstrap.elasticsearch.ESChangeListener;
import org.atlasapi.persistence.content.elasticsearch.ESContentIndexer;
import org.atlasapi.persistence.bootstrap.cassandra.CassandraChangeListener;
import org.atlasapi.persistence.content.cassandra.CassandraContentGroupStore;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.persistence.content.cassandra.CassandraProductStore;
import org.atlasapi.persistence.content.people.cassandra.CassandraPersonStore;
import org.atlasapi.persistence.lookup.cassandra.CassandraLookupEntryStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelGroupStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelStore;
import org.atlasapi.persistence.media.segment.cassandra.CassandraSegmentStore;
import org.atlasapi.persistence.topic.cassandra.CassandraTopicStore;
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
    private final ExecutorService scheduler = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    //
    //
    private ContentBootstrapper cassandraContentBootstrapper;
    private ContentBootstrapper esContentBootstrapper;
    //
    private CassandraContentStore cassandraContentStore;
    private CassandraChannelGroupStore cassandraChannelGroupStore;
    private CassandraChannelStore cassandraChannelStore;
    private CassandraContentGroupStore cassandraContentGroupStore;
    private CassandraPersonStore cassandraPersonStore;
    private CassandraProductStore cassandraProductStore;
    private CassandraSegmentStore cassandraSegmentStore;
    private CassandraTopicStore cassandraTopicStore;
    private CassandraLookupEntryStore cassandraLookupEntryStore;
    private ESContentIndexer esContentIndexer;

    public void setCassandraContentBootstrapper(ContentBootstrapper cassandraContentBootstrapper) {
        this.cassandraContentBootstrapper = cassandraContentBootstrapper;
    }

    public void setEsContentBootstrapper(ContentBootstrapper esContentBootstrapper) {
        this.esContentBootstrapper = esContentBootstrapper;
    }

    public void setCassandraChannelGroupStore(CassandraChannelGroupStore cassandraChannelGroupStore) {
        this.cassandraChannelGroupStore = cassandraChannelGroupStore;
    }

    public void setCassandraChannelStore(CassandraChannelStore cassandraChannelStore) {
        this.cassandraChannelStore = cassandraChannelStore;
    }

    public void setCassandraContentGroupStore(CassandraContentGroupStore cassandraContentGroupStore) {
        this.cassandraContentGroupStore = cassandraContentGroupStore;
    }

    public void setCassandraContentStore(CassandraContentStore cassandraContentStore) {
        this.cassandraContentStore = cassandraContentStore;
    }

    public void setCassandraPersonStore(CassandraPersonStore cassandraPersonStore) {
        this.cassandraPersonStore = cassandraPersonStore;
    }

    public void setCassandraProductStore(CassandraProductStore cassandraProductStore) {
        this.cassandraProductStore = cassandraProductStore;
    }

    public void setCassandraSegmentStore(CassandraSegmentStore cassandraSegmentStore) {
        this.cassandraSegmentStore = cassandraSegmentStore;
    }

    public void setCassandraTopicStore(CassandraTopicStore cassandraTopicStore) {
        this.cassandraTopicStore = cassandraTopicStore;
    }

    public void setCassandraLookupEntryStore(CassandraLookupEntryStore cassandraLookupEntryStore) {
        this.cassandraLookupEntryStore = cassandraLookupEntryStore;
    }
    
    public void setEsContentIndexer(ESContentIndexer esContentIndexer) {
        this.esContentIndexer = esContentIndexer;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra")
    public void bootstrapCassandra(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraChannelGroupStore(cassandraChannelGroupStore);
        cassandraChangeListener.setCassandraChannelStore(cassandraChannelStore);
        cassandraChangeListener.setCassandraContentGroupStore(cassandraContentGroupStore);
        cassandraChangeListener.setCassandraContentStore(cassandraContentStore);
        cassandraChangeListener.setCassandraPersonStore(cassandraPersonStore);
        cassandraChangeListener.setCassandraProductStore(cassandraProductStore);
        cassandraChangeListener.setCassandraSegmentStore(cassandraSegmentStore);
        cassandraChangeListener.setCassandraTopicStore(cassandraTopicStore);
        cassandraChangeListener.setCassandraLookupEntryStore(cassandraLookupEntryStore);
        doBootstrap(cassandraContentBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/es")
    public void bootstrapES(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        ESChangeListener esChangeListener = new ESChangeListener(getConcurrencyLevel(concurrency, response));
        esChangeListener.setESContentIndexer(esContentIndexer);
        doBootstrap(esContentBootstrapper, esChangeListener, response);
    }

    private void doBootstrap(final ContentBootstrapper contentBootstrapper, final ChangeListener changeListener, HttpServletResponse response) throws IOException {
        try {
            scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    boolean bootstrapping = contentBootstrapper.loadAllIntoListener(changeListener);
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
    
    private int getConcurrencyLevel(String concurrency, HttpServletResponse response) throws IOException {
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
        return concurrencyLevel;
    }
}
