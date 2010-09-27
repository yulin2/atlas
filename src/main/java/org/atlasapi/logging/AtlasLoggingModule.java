package org.atlasapi.logging;

import org.atlasapi.logging.www.LogViewingController;
import org.atlasapi.persistence.logging.MongoLoggingAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.webapp.health.HealthController;
import com.metabroadcast.common.webapp.health.HealthProbe;
import com.metabroadcast.common.webapp.health.probes.DiskSpaceProbe;
import com.metabroadcast.common.webapp.health.probes.MemoryInfoProbe;
import com.mongodb.Mongo;

@Configuration
public class AtlasLoggingModule {
	
	private @Autowired Mongo mongo;
	
	public @Bean MongoLoggingAdapter adapterLog() {
		return new MongoLoggingAdapter(new DatabasedMongo(mongo, "atlas"));
	}
	
	public @Bean LogViewingController logView() {
		return new LogViewingController(adapterLog());
	}
	
	public @Bean HealthController healthController() {
		return new HealthController(ImmutableList.<HealthProbe>of(new MemoryInfoProbe(), new DiskSpaceProbe()));
	}
}
