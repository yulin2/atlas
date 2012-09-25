package org.atlasapi.system;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.probes.DiskSpaceProbe;
import com.metabroadcast.common.health.probes.MemoryInfoProbe;
import com.metabroadcast.common.persistence.mongo.health.MongoConnectionPoolProbe;
import com.metabroadcast.common.webapp.health.HealthController;
import org.atlasapi.messaging.producers.MessageReplayer;
import org.atlasapi.persistence.bootstrap.ContentBootstrapper;
import org.atlasapi.persistence.content.cassandra.CassandraContentGroupStore;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.persistence.content.cassandra.CassandraProductStore;
import org.atlasapi.persistence.content.elasticsearch.ESContentIndexer;
import org.atlasapi.persistence.content.people.cassandra.CassandraPersonStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelGroupStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelStore;
import org.atlasapi.persistence.media.segment.cassandra.CassandraSegmentStore;
import org.atlasapi.persistence.topic.cassandra.CassandraTopicStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemModule {
	
	private final ImmutableList<HealthProbe> systemProbes = ImmutableList.<HealthProbe>of(new MemoryInfoProbe(), new DiskSpaceProbe(), new MongoConnectionPoolProbe());
	
	private @Autowired Collection<HealthProbe> probes;
	private @Autowired HealthController healthController;
    private @Autowired MessageReplayer messageReplayer;
    private @Autowired CassandraContentStore cassandraContentStore;
    private @Autowired CassandraChannelGroupStore cassandraChannelGroupStore;
    private @Autowired CassandraChannelStore cassandraChannelStore;
    private @Autowired CassandraContentGroupStore cassandraContentGroupStore;
    private @Autowired CassandraPersonStore cassandraPersonStore;
    private @Autowired CassandraProductStore cassandraProductStore;
    private @Autowired CassandraSegmentStore cassandraSegmentStore;
    private @Autowired CassandraTopicStore cassandraTopicStore;
    private @Autowired ESContentIndexer esContentIndexer;
    private @Autowired @Qualifier("cassandra") ContentBootstrapper cassandraContentBootstrapper;
    private @Autowired @Qualifier("es") ContentBootstrapper esContentBootstrapper;

	public @Bean HealthController healthController() {
		return new HealthController(systemProbes);
	}
	
	public @Bean org.atlasapi.system.HealthController threadController() {
		return new org.atlasapi.system.HealthController();
	}
    
    public @Bean ReplayController replayController() {
        return new ReplayController(messageReplayer);
    }
    
    public @Bean BootstrapController bootstrapController() {
        BootstrapController bootstrapController = new BootstrapController();
        bootstrapController.setCassandraChannelGroupStore(cassandraChannelGroupStore);
        bootstrapController.setCassandraChannelStore(cassandraChannelStore);
        bootstrapController.setCassandraContentGroupStore(cassandraContentGroupStore);
        bootstrapController.setCassandraContentStore(cassandraContentStore);
        bootstrapController.setCassandraPersonStore(cassandraPersonStore);
        bootstrapController.setCassandraProductStore(cassandraProductStore);
        bootstrapController.setCassandraSegmentStore(cassandraSegmentStore);
        bootstrapController.setCassandraTopicStore(cassandraTopicStore);
        bootstrapController.setEsContentIndexer(esContentIndexer);
        bootstrapController.setCassandraContentBootstrapper(cassandraContentBootstrapper);
        bootstrapController.setEsContentBootstrapper(esContentBootstrapper);
        return bootstrapController;
    }
	
	@PostConstruct
	public void addProbes() {
		healthController.addProbes(probes);
	}
}
