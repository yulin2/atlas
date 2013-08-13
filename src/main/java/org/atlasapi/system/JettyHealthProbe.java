package org.atlasapi.system;

import org.atlasapi.AtlasMain;

import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;


public class JettyHealthProbe implements HealthProbe {

    @Override
    public ProbeResult probe() throws Exception {
        ProbeResult probeResult = new ProbeResult("Requests");
        probeResult.add("request-count", String.valueOf(AtlasMain.numberOfConnectionsInLastMinute()), true);
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

}
