package org.atlasapi;

import java.io.File;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class AtlasMain {

    private static final boolean IS_PROCESSING = Boolean.parseBoolean(System.getProperty("processing.config"));
    private static final String LOCAL_WAR_DIR = "./src/main/webapp";

    public static void main(String[] args) throws Exception {

        WebAppContext ctx = new WebAppContext(warBase(), "/");

        if (IS_PROCESSING) {
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

    private static Server createServer() {
        Server server = new Server();

        SocketConnector connector = new SocketConnector();

        int port = defaultPort();

        String customPort = System.getProperty("server.port");
        if (customPort != null) {
            port = Integer.parseInt(customPort);
        }

        QueuedThreadPool pool = new QueuedThreadPool(Runtime.getRuntime().availableProcessors() * 2 * 10);
        pool.setName("jetty-request-thread");
        connector.setThreadPool(pool);

        // one acceptor per CPU
        //connector.setAcceptors(Runtime.getRuntime().availableProcessors());
        //connector.setAcceptQueueSize(Runtime.getRuntime().availableProcessors() * 100);

        connector.setRequestBufferSize(1024);
        connector.setResponseHeaderSize(1024);
        connector.setPort(port);


        server.setConnectors(new Connector[]{connector});

        return server;
    }

    private static int defaultPort() {
        return IS_PROCESSING ? 8282 : 8080;
    }
}
