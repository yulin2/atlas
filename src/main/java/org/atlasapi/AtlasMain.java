package org.atlasapi;

import java.io.File;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class AtlasMain {

	private static final String LOCAL_WAR_DIR = "./src/main/webapp";
    private static int port = 8080;

	public static void main(String[] args) throws Exception {
		
		WebAppContext ctx = new WebAppContext(warBase(), "/");
		
		if(Boolean.parseBoolean(System.getProperty("processing.config"))) {
		    System.out.println(">>> Launching processing configuration");
		    port = 8282;
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

	private static Server createServer() {
		Server server = new Server();
		
		final SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		
		connector.setAcceptQueueSize(200);
		
		QueuedThreadPool pool = new QueuedThreadPool(100);
		pool.setName("jetty-request-thread");
		connector.setThreadPool(pool);
		
		// one acceptor per CPU (ish)
		connector.setAcceptors(4);
		
		connector.setRequestBufferSize(1024);
		connector.setResponseHeaderSize(1024);
		
		server.setConnectors(new Connector[] { connector });
		return server;
	}
}
