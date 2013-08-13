package org.atlasapi;

import java.io.File;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.joda.time.Duration;

import com.metabroadcast.common.caching.BackgroundTask;

public class AtlasMain {

	private static final boolean IS_PROCESSING = Boolean.parseBoolean(System.getProperty("processing.config"));
	
    private static final String LOCAL_WAR_DIR = "./src/main/webapp";
    private static SelectChannelConnector CONNECTOR;
    
    private static final BackgroundTask OPEN_CONNECTION_COUNTER_RESET = new BackgroundTask(Duration.standardMinutes(1), new Runnable() {
        
        @Override
        public void run() {
            if (CONNECTOR != null) {
                CONNECTOR.statsReset();
            }
        }
    });

	public static void main(String[] args) throws Exception {
	    if(IS_PROCESSING) {
            System.out.println(">>> Launching processing configuration");
        }

	    OPEN_CONNECTION_COUNTER_RESET.start();
	    createWebApp(warBase() + "/WEB-INF/web.xml", createApiServer());
	    createWebApp(warBase() + "/WEB-INF/web-monitoring.xml", createMonitoringServer());
		
	}
	
	private static void createWebApp(String descriptor, final Server server) throws Exception {
	    WebAppContext ctx = new WebAppContext(warBase(), "/");
        ctx.setDescriptor(descriptor);
        
        server.setHandler(ctx);
        server.start();
	}
	
	private static String warBase() {
		if (new File(LOCAL_WAR_DIR).exists()) {
			return LOCAL_WAR_DIR;
		}
		ProtectionDomain domain = AtlasMain.class.getProtectionDomain();
        return domain.getCodeSource().getLocation().toString();
	}

	private static Server createApiServer() throws Exception {
	    int port = defaultPort();
        
        String customPort = System.getProperty("server.port");
        if (customPort != null) {
            port = Integer.parseInt(customPort);
        }
        
        int requestThreads;
        String requestThreadsString = System.getProperty("request.threads");
        if (requestThreadsString == null) {
            requestThreads = 100;
        } else {
            requestThreads = Integer.parseInt(requestThreadsString);
        }
		Server server = createServer(port, requestThreads, 200, "api-request-thread");
		CONNECTOR = (SelectChannelConnector) server.getConnectors()[0];
		return server;
	}
	
	private static Server createMonitoringServer() throws Exception {
	    int port = 8081;
        
        String customPort = System.getProperty("monitoring.port");
        if (customPort != null) {
            port = Integer.parseInt(customPort);
        }
        
        return createServer(port, 10, 20, "monitoring-request-thread");
    }
	
	private static Server createServer(int port, int maxThreads, int acceptQueueSize, String threadNamePrefix) {
	    Server server = new Server();
        
	    SelectChannelConnector connector = new SelectChannelConnector();
        connector.setStatsOn(true);
        
        connector.setPort(port);
        connector.setAcceptQueueSize(acceptQueueSize);
        
        QueuedThreadPool pool = new QueuedThreadPool(maxThreads);
        pool.setName(threadNamePrefix);
        connector.setThreadPool(pool);
        
        // one acceptor per CPU (ish)
        connector.setAcceptors(4);
        
        connector.setRequestBufferSize(1024);
        connector.setResponseHeaderSize(1024);
        
        server.setConnectors(new Connector[] { connector });
        
        return server;
	}
	

    private static int defaultPort() {
        return IS_PROCESSING ? 8282 : 8080;
    }
    
    public static int numberOfConnectionsInLastMinute() {
        return CONNECTOR.getConnectionsOpen();
    }
}
