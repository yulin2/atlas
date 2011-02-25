package org.atlasapi;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.web.servlet.DispatcherServlet;

public class AtlasProcessingMain {
    
    public static void main(String[] args) throws Exception {
        Server server = createServer();
        ServletContextHandler ctx = new ServletContextHandler();
        
        DispatcherServlet servlet = new DispatcherServlet();

        servlet.setContextClass(AtlasProcessingWebApplicationContext.class);
        
        ctx.addServlet(new ServletHolder(servlet), "/");

        server.setHandler(ctx);
        
        server.start();
        server.join();
    }
 
    private static Server createServer() {
        int port = 8282;
        
        Server server = new Server();
        
        final SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        
        connector.setAcceptQueueSize(2048);
        
        connector.setThreadPool(new QueuedThreadPool(500));
        
        // one acceptor per CPU (ish)
        connector.setAcceptors(4);
        
        connector.setRequestBufferSize(1024);
        connector.setResponseHeaderSize(1024);
        
        server.setConnectors(new Connector[] { connector });
        return server;
    }
    
}
