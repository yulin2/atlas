package org.atlasapi.logging;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.probes.DiskSpaceProbe;
import com.metabroadcast.common.health.probes.MemoryInfoProbe;
import com.metabroadcast.common.webapp.health.HealthController;

public class HealthModule {
	
	private final ImmutableList<HealthProbe> systemProbes = ImmutableList.<HealthProbe>of(new MemoryInfoProbe(), new DiskSpaceProbe());
	
	private @Autowired Collection<HealthProbe> probes;

	public @Bean HealthController healthController() {
		List<HealthProbe> allProbes = Lists.newArrayList(systemProbes);
		allProbes.addAll(probes);
		return new HealthController(allProbes);
	}
}
