package org.atlasapi.system;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.atlasapi.media.content.ContentIndexer;
import org.atlasapi.messaging.producers.MessageReplayer;
import org.atlasapi.persistence.bootstrap.ContentBootstrapper;
import org.atlasapi.persistence.content.cassandra.CassandraContentGroupStore;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.persistence.content.cassandra.CassandraProductStore;
import org.atlasapi.persistence.content.people.cassandra.CassandraPersonStore;
import org.atlasapi.persistence.lookup.cassandra.CassandraLookupEntryStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelGroupStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelStore;
import org.atlasapi.persistence.media.segment.cassandra.CassandraSegmentStore;
import org.atlasapi.persistence.topic.cassandra.CassandraTopicStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.probes.DiskSpaceProbe;
import com.metabroadcast.common.health.probes.MemoryInfoProbe;
import com.metabroadcast.common.persistence.mongo.health.MongoConnectionPoolProbe;
import com.metabroadcast.common.webapp.health.HealthController;

@Configuration
public class SystemModule {
	
	private final ImmutableList<HealthProbe> systemProbes = ImmutableList.<HealthProbe>of(new MemoryInfoProbe(), new DiskSpaceProbe(), new MongoConnectionPoolProbe());
	
	private @Autowired Collection<HealthProbe> probes;
	private @Autowired HealthController healthController;


	public @Bean HealthController healthController() {
		return new HealthController(systemProbes);
	}
	
	public @Bean org.atlasapi.system.HealthController threadController() {
		return new org.atlasapi.system.HealthController();
	}
    
	@PostConstruct
	public void addProbes() {
		healthController.addProbes(probes);
	}
}
