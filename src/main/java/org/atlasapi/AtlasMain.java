package org.atlasapi;

import java.io.File;
import java.security.ProtectionDomain;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class AtlasMain {

	private static final boolean IS_PROCESSING = Boolean.parseBoolean(System.getProperty("processing.config"));
	
    private static final String LOCAL_WAR_DIR = "./src/main/webapp";
    
    private static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

	public static void main(String[] args) throws Exception {
		
		WebAppContext ctx = new WebAppContext(warBase(), "/");
		
		if(IS_PROCESSING) {
		    System.out.println(">>> Launching processing configuration");
		}

		Server server = createServer();
		server.setHandler(ctx);
		
		server.start();
		server.join();
	}
	
	private static String warBase() {
		if (new File(LOCAL_WAR_DIR).exists()) {
			return LOCAL_WAR_DIR;
		}
		ProtectionDomain domain = AtlasMain.class.getProtectionDomain();
        return domain.getCodeSource().getLocation().toString();
	}

	private static Server createServer() throws Exception {
		Server server = new Server();
		
		final SelectChannelConnector connector = new SelectChannelConnector();
		
		int port = defaultPort();
		
		String customPort = System.getProperty("server.port");
        if (customPort != null) {
            port = Integer.parseInt(customPort);
        }
		
        String requestThreadsString = System.getProperty("request.threads");
        int requestThreads;
        if (requestThreadsString == null) {
            requestThreads = 100;
        } else {
            requestThreads = Integer.parseInt(requestThreadsString);
        }
		connector.setPort(port);
		connector.setAcceptQueueSize(200);
		
		QueuedThreadPool pool = new QueuedThreadPool(queue);
		pool.setMaxThreads(requestThreads);
		pool.setName("jetty-request-thread");
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
    
    public static int numberOfRequestsInQueue() {
        return queue.size();
    }
}
