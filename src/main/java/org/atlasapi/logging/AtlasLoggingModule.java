package org.atlasapi.logging;

import java.util.Collection;
import java.util.List;

import org.atlasapi.logging.www.LogViewingController;
import org.atlasapi.persistence.logging.MongoLoggingAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.webapp.health.HealthController;
import com.metabroadcast.common.webapp.health.HealthProbe;
import com.metabroadcast.common.webapp.health.probes.DiskSpaceProbe;
import com.metabroadcast.common.webapp.health.probes.MemoryInfoProbe;
import com.mongodb.Mongo;

@Configuration
public class AtlasLoggingModule {
	
	private @Autowired Mongo mongo;
	private @Autowired Collection<HealthProbe> probes;
	
	public @Bean MongoLoggingAdapter adapterLog() {
		return new MongoLoggingAdapter(new DatabasedMongo(mongo, "atlas"));
	}
	
	public @Bean LogViewingController logView() {
		return new LogViewingController(adapterLog());
	}
	
	private final ImmutableList<HealthProbe> systemProbes = ImmutableList.<HealthProbe>of(new MemoryInfoProbe(), new DiskSpaceProbe());
	
	public @Bean HealthController healthController() {
		List<HealthProbe> allProbes = Lists.newArrayList(systemProbes);
		allProbes.addAll(probes);
		return new HealthController(allProbes);
	}
}
