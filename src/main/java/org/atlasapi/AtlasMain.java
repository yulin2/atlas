package org.atlasapi;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class AtlasMain implements Runnable {

    public static final String CONTEXT_ATTRIBUTE = "ATLAS_MAIN";
    private static final String LOCAL_WAR_DIR = "./src/main/webapp";
    
    private static final ScheduledExecutorService CONNECTOR_RESET_THREAD_SERVICE = new ScheduledThreadPoolExecutor(1);
	private static final boolean IS_PROCESSING = Boolean.parseBoolean(System.getProperty("processing.config"));
	
    private SelectChannelConnector apiConnector;
    
	public static void main(String[] args) throws Exception {
	    if(IS_PROCESSING) {
            System.out.println(">>> Launching processing configuration");
        }
	    new AtlasMain().start();
	}
	
	public void start() throws Exception {
	    WebAppContext apiContext = createWebApp(warBase() + "/WEB-INF/web.xml", createApiServer());
        WebAppContext monitoringContext = createWebApp(warBase() + "/WEB-INF/web-monitoring.xml", createMonitoringServer());
        monitoringContext.setAttribute(CONTEXT_ATTRIBUTE, this);
        apiContext.setAttribute(CONTEXT_ATTRIBUTE, this);
        
        CONNECTOR_RESET_THREAD_SERVICE.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
	}
	
	@Override
    public void run() {
        if (apiConnector != null) {
            apiConnector.statsReset();
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
        String requestThreadsString = System.getProperty("request.threads");
        if (requestThreadsString == null) {
            requestThreads = 100;
        } else {
            requestThreads = Integer.parseInt(requestThreadsString);
        }

		Server server = createServer("server.port", defaultPort(), requestThreads, 200, 
		        Runtime.getRuntime().availableProcessors(), "api-request-thread");
		apiConnector = (SelectChannelConnector) server.getConnectors()[0];
		return server;
	}
	
	private Server createMonitoringServer() throws Exception {
        return createServer("monitoring.port", 8081, 10, 20, 1, "monitoring-request-thread");
    }
	
	private Server createServer(String portProperty, int defaultPort, int maxThreads, int acceptors, 
	        int acceptQueueSize, String threadNamePrefix) {
	    Server server = new Server();
        
	    SelectChannelConnector connector = new SelectChannelConnector();
        connector.setStatsOn(true);
        
        int port = defaultPort;
        String customPort = System.getProperty(portProperty);
        if (customPort != null) {
            port = Integer.parseInt(customPort);
        }
        
        connector.setPort(port);
        connector.setAcceptQueueSize(acceptQueueSize);
        
        QueuedThreadPool pool = new QueuedThreadPool(maxThreads);
        pool.setName(threadNamePrefix);
        connector.setThreadPool(pool);
        
        connector.setAcceptors(acceptors);
        
        connector.setRequestBufferSize(1024);
        connector.setResponseHeaderSize(1024);
        
        server.setConnectors(new Connector[] { connector });
        
        return server;
	}
	
    private int defaultPort() {
        return IS_PROCESSING ? 8282 : 8080;
    }
    
    public int getNumberOfConnectionsMax() {
        return apiConnector.getConnectionsOpenMax();
    }

    public static int getMaxNumberOfOpenConnectionsInLastMinute(Object atlasMain) throws IllegalArgumentException, 
                            SecurityException, IllegalAccessException, InvocationTargetException {
        Class<? extends Object> clazz = atlasMain.getClass();
        if (clazz.getCanonicalName() != AtlasMain.class.getCanonicalName()) {
            throw new IllegalArgumentException("Parameter must be instance of " + AtlasMain.class.getCanonicalName());
        }
        
        try {
            return (Integer) clazz.getDeclaredMethod("getNumberOfConnectionsMax").invoke(atlasMain);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There appears to be a mismatch between AtlasMain objects", e);
        } 
    }
}
