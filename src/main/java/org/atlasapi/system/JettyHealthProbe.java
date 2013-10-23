package org.atlasapi.system;

import javax.servlet.ServletContext;

import org.atlasapi.AtlasMain;
import org.springframework.web.context.ServletContextAware;

import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;


public class JettyHealthProbe implements HealthProbe, ServletContextAware {

    private ServletContext servletContext;

    @Override
    public ProbeResult probe() throws Exception {
        ProbeResult probeResult = new ProbeResult("Requests");
        String connections = AtlasMain.getMaxNumberOfOpenConnectionsInLastMinute(
                servletContext.getAttribute(AtlasMain.CONTEXT_ATTRIBUTE));
        probeResult.add("utilisation", connections, true);
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
    }

}
