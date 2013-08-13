package org.atlasapi.system;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.springframework.web.context.ServletContextAware;

import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;


public class JettyHealthProbe implements HealthProbe, ServletContextAware {

    private ServletContext servletContext;

    @Override
    public ProbeResult probe() throws Exception {
        ProbeResult probeResult = new ProbeResult("Requests");
        SelectChannelConnector connector = (SelectChannelConnector) servletContext.getAttribute("CONNECTOR");
        probeResult.add("request-count", String.valueOf(connector.getConnectionsOpen()), true);
        return probeResult;
    }

    @Override
    public String title() {
        return "Jetty";
    }

    @Override
    public String slug() {
        return "jetty";
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        // TODO Auto-generated method stub
        
    }

}
