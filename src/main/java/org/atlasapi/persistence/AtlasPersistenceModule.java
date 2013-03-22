package org.atlasapi.persistence;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.atlasapi.messaging.AtlasMessagingModule;
import org.atlasapi.equiv.CassandraEquivalenceSummaryStore;
import org.atlasapi.media.CassandraPersistenceModule;
import org.atlasapi.media.ElasticSearchContentIndexModule;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupStore;
import org.atlasapi.media.channel.MongoChannelGroupStore;
import org.atlasapi.media.channel.MongoChannelStore;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.content.EsContentIndex;
import org.atlasapi.media.content.EsContentIndexer;
import org.atlasapi.media.content.EsContentSearcher;
import org.atlasapi.media.content.schedule.EsScheduleIndex;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.segment.Segment;
import org.atlasapi.media.segment.SegmentRef;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.media.segment.SegmentWriter;
import org.atlasapi.media.topic.EsPopularTopicIndex;
import org.atlasapi.media.topic.EsTopicIndex;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.messaging.MessageQueueingContentStore;
import org.atlasapi.messaging.MessageQueueingTopicStore;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
import org.atlasapi.persistence.media.TranslatorContentHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.IdGeneratorBuilder;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;


@Configuration
public class AtlasPersistenceModule {

    private final String mongoHost = Configurer.get("mongo.host").get();
    private final String mongoDbName = Configurer.get("mongo.dbName").get();
    
    private final String cassandraCluster = Configurer.get("cassandra.cluster").get();
    private final String cassandraKeyspace = Configurer.get("cassandra.keyspace").get();
    private final String cassandraSeeds = Configurer.get("cassandra.seeds").get();
    private final String cassandraPort = Configurer.get("cassandra.port").get();
    private final String cassandraConnectionTimeout = Configurer.get("cassandra.connectionTimeout").get();
    private final String cassandraClientThreads = Configurer.get("cassandra.clientThreads").get();
 
    private final String esSeeds = Configurer.get("elasticsearch.seeds").get();
    private final String esRequestTimeout = Configurer.get("elasticsearch.requestTimeout").get();
    private final Parameter processingConfig = Configurer.get("processing.config");

    @Resource(name = "contentChanges") private JmsTemplate contentChanges;
    @Resource(name = "topicChanges") private JmsTemplate topicChanges;

    @PostConstruct
    public void init() {
        persistenceModule().start();
    }

    @Bean
    public CassandraPersistenceModule persistenceModule() {
        return new CassandraPersistenceModule(Splitter.on(",").split(cassandraSeeds), 
            Integer.parseInt(cassandraPort), cassandraCluster, cassandraKeyspace, 
            Integer.parseInt(cassandraClientThreads), Integer.parseInt(cassandraConnectionTimeout), 
            idGeneratorBuilder(), new TranslatorContentHasher());
    }
    
    @Bean
    public ContentStore contentStore() {
        return new MessageQueueingContentStore(contentChanges, 
            persistenceModule().contentStore());
    }
    
    @Bean TopicStore topicStore() {
        return new MessageQueueingTopicStore(topicChanges,
            persistenceModule().topicStore());
    }

    @Bean
    public ElasticSearchContentIndexModule esContentIndexModule() {
        return new ElasticSearchContentIndexModule(esSeeds, Long.parseLong(esRequestTimeout));
    }

    @Bean @Primary
    public DatabasedMongo databasedMongo() {
        return new DatabasedMongo(mongo(), mongoDbName);
    }

    @Bean @Primary
    public Mongo mongo() {
        Mongo mongo = new Mongo(mongoHosts());
        if (processingConfig == null || !processingConfig.toBoolean()) {
            mongo.setReadPreference(ReadPreference.secondaryPreferred());
        }
        return mongo;
    }
    
    @Bean
    public IdGeneratorBuilder idGeneratorBuilder() {
        return new IdGeneratorBuilder() {

            @Override
            public IdGenerator generator(String sequenceIdentifier) {
                return new MongoSequentialIdGenerator(databasedMongo(), sequenceIdentifier);
            }
        };
    }

    @Bean
    @Primary
    public EsContentIndexer contentIndexer() {
        return esContentIndexModule().contentIndexer();
    }

    @Bean
    @Primary
    public EsScheduleIndex scheduleIndex() {
        return esContentIndexModule().scheduleIndex();
    }

    @Bean
    @Primary
    public EsContentIndex contentIndex() {
        return esContentIndexModule().contentIndex();
    }

    @Bean
    @Primary
    public EsTopicIndex topicIndex() {
        return esContentIndexModule().topicIndex();
    }

    @Bean
    @Primary
    public EsPopularTopicIndex popularTopicIndex() {
        return esContentIndexModule().topicSearcher();
    }
    
    @Bean
    @Primary
    public EsContentSearcher contentSearcher() {
        return esContentIndexModule().contentSearcher();
    }

    @Bean
    @Primary
    public MongoChannelStore cassandraChannelStore() {
        return new MongoChannelStore(databasedMongo(), channelGroupStore(), channelGroupStore());
    }
    
    @Bean
    @Primary
    private ChannelGroupStore channelGroupStore() {
        return new MongoChannelGroupStore(databasedMongo());
    }

    private List<ServerAddress> mongoHosts() {
        Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(splitter.split(mongoHost), new Function<String, ServerAddress>() {

            @Override
            public ServerAddress apply(String input) {
                try {
                    return new ServerAddress(input, 27017);
                } catch (UnknownHostException e) {
                    return null;
                }
            }
        }), Predicates.notNull()));
    }
    
    @Bean
    public ItemsPeopleWriter nullPeopleWriter() {
        return new ItemsPeopleWriter() {
            @Override
            public void createOrUpdatePeople(Item item) {
                //no-op
            }
        };
    }
    
    @Bean
    public SegmentWriter nullSegmentWriter() {
        return new SegmentWriter() {
            
            @Override
            public Segment write(Segment segment) {
                return segment;
            }
        };
    }
}
