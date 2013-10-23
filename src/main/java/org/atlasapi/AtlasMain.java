package org.atlasapi;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.text.DecimalFormat;
import java.util.concurrent.Executor;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.webapp.WebAppContext;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;

public class AtlasMain {

    private static final String METRIC_METHOD_NAME = "getMetric";
    private static final String SERVER_REQUEST_THREADS_OVERRIDE_PROPERTY_NAME = "request.threads";
    private static final int DEFAULT_SERVER_REQUEST_THREADS = 100;
    private static final String SERVER_REQUEST_THREAD_PREFIX = "api-request-thread";
    private static final int SERVER_ACCEPT_QUEUE_SIZE = 200;
    
    private static final String SERVER_PORT_OVERRIDE_PROPERTY_NAME = "server.port";
    private static final int API_DEFAULT_PORT = 8080;
    private static final int PROCESSING_DEFAULT_PORT = 8282;
    
    private static final int MONITORING_REQUEST_THREADS = 20;
    private static final String MONITORING_REQUEST_THREAD_PREFIX = "monitoring-request-thread";
    private static final int MONITORING_DEFAULT_PORT = 8081;
    private static final String MONITORING_PORT_OVERRIDE_PROPERTY_NAME = "monitoring.port";
    
    private static final String METRIC_UTILIZATION_GUAGE_NAME = "utilization";
    
    public static final String CONTEXT_ATTRIBUTE = "ATLAS_MAIN";
    private static final String LOCAL_WAR_DIR = "./src/main/webapp";

    private static final boolean IS_PROCESSING = Boolean.parseBoolean(
                                                    System.getProperty("processing.config")
                                                 );

    private final MetricRegistry metrics = new MetricRegistry();
    
    public static void main(String[] args) throws Exception {
        if (IS_PROCESSING) {
            System.out.println(">>> Launching processing configuration");
        }
        new AtlasMain().start();
    }

    public void start() throws Exception {
       
        WebAppContext apiContext = createWebApp(warBase() + "/WEB-INF/web.xml", createApiServer());
        apiContext.setAttribute(CONTEXT_ATTRIBUTE, this);
        if (!IS_PROCESSING) {
            WebAppContext monitoringContext = createWebApp(warBase() + "/WEB-INF/web-monitoring.xml",
                    createMonitoringServer());
            monitoringContext.setAttribute(CONTEXT_ATTRIBUTE, this);
        }
    }

    private WebAppContext createWebApp(String descriptor, final Server server) throws Exception {
        WebAppContext ctx = new WebAppContext(warBase(), "/");
        ctx.setDescriptor(descriptor);
        server.setHandler(ctx);
        server.start();
        
        return ctx;
    }

    private String warBase() {
        if (new File(LOCAL_WAR_DIR).exists()) {
            return LOCAL_WAR_DIR;
        }
        ProtectionDomain domain = AtlasMain.class.getProtectionDomain();
        return domain.getCodeSource().getLocation().toString();
    }

    private Server createApiServer() throws Exception {
        int requestThreads;
        String requestThreadsString = System.getProperty(SERVER_REQUEST_THREADS_OVERRIDE_PROPERTY_NAME);
        if (requestThreadsString == null) {
            requestThreads = DEFAULT_SERVER_REQUEST_THREADS;
        } else {
            requestThreads = Integer.parseInt(requestThreadsString);
        }

        return createServer(defaultPort(), SERVER_PORT_OVERRIDE_PROPERTY_NAME, requestThreads, 
                SERVER_ACCEPT_QUEUE_SIZE, SERVER_REQUEST_THREAD_PREFIX);
    }

    private Server createMonitoringServer() throws Exception {
        int defaultAcceptQueueSize = 0;
        return createServer(MONITORING_DEFAULT_PORT, MONITORING_PORT_OVERRIDE_PROPERTY_NAME, 
                MONITORING_REQUEST_THREADS, defaultAcceptQueueSize, MONITORING_REQUEST_THREAD_PREFIX);
    }

    private Server createServer(int defaultPort, String portPropertyName, int maxThreads,
            int acceptQueueSize, String threadNamePrefix) {
         
        Server server = new Server(createRequestThreadPool(maxThreads, threadNamePrefix));
        createServerConnector(server, createHttpConnectionFactory(), defaultPort, portPropertyName, 
                acceptQueueSize);

        return server;
    }
    
    private QueuedThreadPool createRequestThreadPool(int maxThreads, String threadNamePrefix) {
        QueuedThreadPool pool = new InstrumentedQueuedThreadPool(metrics, maxThreads);
        pool.setName(threadNamePrefix);
        
        return pool;
    }

    // The lifecycle of ServerConnector is handled by Server
    @SuppressWarnings("resource")
    private void createServerConnector(Server server, HttpConnectionFactory connectionFactory,
            int defaultPort, String portPropertyName, int acceptQueueSize) {
        
        int acceptors = Runtime.getRuntime().availableProcessors();
        Executor defaultExecutor = null;
        Scheduler defaultScheduler = null;
        ByteBufferPool defaultByteBufferPool = null;
        int selectors = 0;
        
        ServerConnector connector = new ServerConnector(server, defaultExecutor, defaultScheduler, 
                defaultByteBufferPool, acceptors, selectors, connectionFactory);
        
        connector.setPort(getPort(defaultPort, portPropertyName));
        connector.setAcceptQueueSize(acceptQueueSize);
    }
    
    private HttpConnectionFactory createHttpConnectionFactory() {
        HttpConfiguration config = new HttpConfiguration();
        config.setRequestHeaderSize(1024);
        config.setResponseHeaderSize(1024);
        
        return new HttpConnectionFactory(config);
    }
    
    private int getPort(int defaultPort, String portProperty) {
        String customPort = System.getProperty(portProperty);
        if (customPort != null) {
            return Integer.parseInt(customPort);
        }
        return defaultPort;
    }

    private int defaultPort() {
        return IS_PROCESSING ? PROCESSING_DEFAULT_PORT : API_DEFAULT_PORT;
    }

    @SuppressWarnings("unchecked")
    public String getMetric() {
        Gauge<Double> gauge = metrics.getGauges().get(MetricRegistry.name(QueuedThreadPool.class, 
                SERVER_REQUEST_THREAD_PREFIX + "." + METRIC_UTILIZATION_GUAGE_NAME));
        Double value = gauge.getValue();
        return new DecimalFormat("0.00").format(value);
    }

    public static String getMaxNumberOfOpenConnectionsInLastMinute(Object atlasMain)
            throws IllegalArgumentException,
            SecurityException, IllegalAccessException, InvocationTargetException {
        Class<? extends Object> clazz = atlasMain.getClass();
        if (clazz.getCanonicalName() != AtlasMain.class.getCanonicalName()) {
            throw new IllegalArgumentException("Parameter must be instance of "
                + AtlasMain.class.getCanonicalName());
        }

        try {
            return (String) clazz.getDeclaredMethod(METRIC_METHOD_NAME).invoke(atlasMain);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Couldn't find method " + METRIC_METHOD_NAME + ": Perhaps a mismatch between AtlasMain objects across classloaders?",
                    e);
        }
    }
}
