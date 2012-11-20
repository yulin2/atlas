package org.atlasapi.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
<<<<<<< HEAD
=======
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

>>>>>>> 7fe6209... rename ES* source to Es*
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
import org.atlasapi.persistence.content.elasticsearch.EsContentIndexer;
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

<<<<<<< HEAD
=======
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

>>>>>>> 7fe6209... rename ES* source to Es*
/**
 */
@Controller
public class BootstrapController {

    private static final Log log = LogFactory.getLog(BootstrapController.class);
    //
    private final ExecutorService scheduler = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
    private final ObjectMapper jsonMapper = new ObjectMapper();
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
    private EsContentIndexer esContentIndexer;
    //
    private ContentBootstrapper cassandraContentBootstrapper;
    private ContentBootstrapper cassandraChannelBootstrapper;
    private ContentBootstrapper cassandraContentGroupBootstrapper;
    private ContentBootstrapper cassandraPeopleBootstrapper;
    private ContentBootstrapper cassandraProductBootstrapper;
    private ContentBootstrapper cassandraSegmentBootstrapper;
    private ContentBootstrapper cassandraTopicBootstrapper;
    private ContentBootstrapper esContentBootstrapper;
    
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

    public void setEsContentIndexer(EsContentIndexer esContentIndexer) {
        this.esContentIndexer = esContentIndexer;
    }

    public void setCassandraChannelBootstrapper(ContentBootstrapper cassandraChannelBootstrapper) {
        this.cassandraChannelBootstrapper = cassandraChannelBootstrapper;
    }

    public void setCassandraContentBootstrapper(ContentBootstrapper cassandraContentBootstrapper) {
        this.cassandraContentBootstrapper = cassandraContentBootstrapper;
    }

    public void setCassandraContentGroupBootstrapper(ContentBootstrapper cassandraContentGroupBootstrapper) {
        this.cassandraContentGroupBootstrapper = cassandraContentGroupBootstrapper;
    }

    public void setCassandraPeopleBootstrapper(ContentBootstrapper cassandraPeopleBootstrapper) {
        this.cassandraPeopleBootstrapper = cassandraPeopleBootstrapper;
    }

    public void setCassandraProductBootstrapper(ContentBootstrapper cassandraProductBootstrapper) {
        this.cassandraProductBootstrapper = cassandraProductBootstrapper;
    }

    public void setCassandraSegmentBootstrapper(ContentBootstrapper cassandraSegmentBootstrapper) {
        this.cassandraSegmentBootstrapper = cassandraSegmentBootstrapper;
    }

    public void setCassandraTopicBootstrapper(ContentBootstrapper cassandraTopicBootstrapper) {
        this.cassandraTopicBootstrapper = cassandraTopicBootstrapper;
    }

    public void setEsContentBootstrapper(ContentBootstrapper esContentBootstrapper) {
        this.esContentBootstrapper = esContentBootstrapper;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/content")
    public void bootstrapCassandraContent(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraContentStore(cassandraContentStore);
        cassandraChangeListener.setCassandraLookupEntryStore(cassandraLookupEntryStore);
        doBootstrap(cassandraContentBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/contentGroup")
    public void bootstrapCassandraContentGroup(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraContentGroupStore(cassandraContentGroupStore);
        doBootstrap(cassandraContentGroupBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/channel")
    public void bootstrapCassandraChannel(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraChannelGroupStore(cassandraChannelGroupStore);
        cassandraChangeListener.setCassandraChannelStore(cassandraChannelStore);
        doBootstrap(cassandraChannelBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/people")
    public void bootstrapCassandraPeople(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraPersonStore(cassandraPersonStore);
        doBootstrap(cassandraPeopleBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/product")
    public void bootstrapCassandra(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraProductStore(cassandraProductStore);
        doBootstrap(cassandraProductBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/segment")
    public void bootstrapCassandraSegment(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraSegmentStore(cassandraSegmentStore);
        doBootstrap(cassandraSegmentBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/cassandra/topic")
    public void bootstrapCassandraTopic(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        CassandraChangeListener cassandraChangeListener = new CassandraChangeListener(getConcurrencyLevel(concurrency, response));
        cassandraChangeListener.setCassandraTopicStore(cassandraTopicStore);
        doBootstrap(cassandraTopicBootstrapper, cassandraChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/system/bootstrap/es")
    public void bootstrapES(@RequestParam(required = false) String concurrency, HttpServletResponse response) throws IOException {
        ESChangeListener esChangeListener = new ESChangeListener(getConcurrencyLevel(concurrency, response));
        esChangeListener.setESContentIndexer(esContentIndexer);
        doBootstrap(esContentBootstrapper, esChangeListener, response);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/content/status")
    public void cassandraContentBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraContentBootstrapper, response);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/channel/status")
    public void cassandraChannelBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraChannelBootstrapper, response);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/contentGroup/status")
    public void cassandraContentGroupBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraContentGroupBootstrapper, response);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/people/status")
    public void cassandraPeopleBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraPeopleBootstrapper, response);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/product/status")
    public void cassandraProductBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraProductBootstrapper, response);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/segment/status")
    public void cassandraSegmentBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraSegmentBootstrapper, response);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/cassandra/topic/status")
    public void cassandraTopicBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(cassandraTopicBootstrapper, response);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/system/bootstrap/es/content/status")
    public void esContentBootstrapStatus(HttpServletResponse response) throws IOException {
        writeBootstrapStatus(esContentBootstrapper, response);
    }

    private void doBootstrap(final ContentBootstrapper contentBootstrapper, final ChangeListener changeListener, HttpServletResponse response) throws IOException {
        try {
            scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    boolean bootstrapping = contentBootstrapper.loadAllIntoListener(changeListener);
                    if (!bootstrapping) {
                        log.warn("Bootstrapping failed because apparently busy bootstrapping something else.");
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Currently bootstrapping another component.");
        }
    }

    public void writeBootstrapStatus(ContentBootstrapper contentBootstrapper, HttpServletResponse response) throws IOException {
        Map<String, Object> result = Maps.newHashMap();
        result.put("bootstrapping", contentBootstrapper.isBootstrapping());
        result.put("lastStatus", contentBootstrapper.getLastStatus());
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
