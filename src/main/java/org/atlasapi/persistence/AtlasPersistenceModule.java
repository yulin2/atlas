package org.atlasapi.persistence;

import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.Resource;

import org.atlasapi.messaging.AtlasMessagingModule;
import org.atlasapi.media.content.util.MessageQueueingContentWriter;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.EquivalenceWritingContentWriter;
import org.atlasapi.persistence.content.IdSettingContentWriter;
import org.atlasapi.persistence.content.LookupResolvingContentResolver;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.persistence.content.elasticsearch.ESContentIndexer;
import org.atlasapi.persistence.content.elasticsearch.EsScheduleIndex;
import org.atlasapi.persistence.content.mongo.MongoContentGroupResolver;
import org.atlasapi.persistence.content.mongo.MongoContentGroupWriter;
import org.atlasapi.persistence.content.mongo.MongoContentLister;
import org.atlasapi.persistence.content.mongo.MongoContentResolver;
import org.atlasapi.persistence.content.mongo.MongoPersonStore;
import org.atlasapi.persistence.content.mongo.MongoProductStore;
import org.atlasapi.persistence.content.people.QueuingItemsPeopleWriter;
import org.atlasapi.persistence.content.schedule.mongo.MongoScheduleStore;
import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.atlasapi.persistence.media.channel.MongoChannelGroupStore;
import org.atlasapi.persistence.media.channel.MongoChannelStore;
import org.atlasapi.persistence.media.segment.IdSettingSegmentWriter;
import org.atlasapi.persistence.media.segment.MongoSegmentResolver;
import org.atlasapi.persistence.messaging.mongo.MongoMessageStore;
import org.atlasapi.persistence.shorturls.MongoShortUrlSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.persistence.topic.TopicCreatingTopicResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.IdGeneratorBuilder;
import com.metabroadcast.common.ids.UUIDGenerator;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.health.MongoIOProbe;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;
import com.mongodb.Mongo;
import com.mongodb.MongoReplicaSetProbe;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.atlasapi.persistence.bootstrap.ContentBootstrapper;
import org.atlasapi.persistence.content.elasticsearch.ESContentSearcher;
import org.atlasapi.persistence.content.cassandra.CassandraContentGroupStore;
import org.atlasapi.persistence.content.cassandra.CassandraProductStore;
import org.atlasapi.persistence.content.people.cassandra.CassandraPersonStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelGroupStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelStore;
import org.atlasapi.persistence.media.segment.cassandra.CassandraSegmentStore;
import org.atlasapi.persistence.topic.cassandra.CassandraTopicStore;
import org.atlasapi.persistence.topic.elasticsearch.ESTopicSearcher;

@Configuration
public class AtlasPersistenceModule {

    private final String mongoHost = Configurer.get("mongo.host").get();
    private final String mongoDbName = Configurer.get("mongo.dbName").get();
    private final String cassandraEnv = Configurer.get("cassandra.env").get();
    private final String cassandraSeeds = Configurer.get("cassandra.seeds").get();
    private final String cassandraPort = Configurer.get("cassandra.port").get();
    private final String cassandraConnectionTimeout = Configurer.get("cassandra.connectionTimeout").get();
    private final String cassandraRequestTimeout = Configurer.get("cassandra.requestTimeout").get();
    private final String esSeeds = Configurer.get("elasticsearch.seeds").get();
    private final String esRequestTimeout = Configurer.get("elasticsearch.requestTimeout").get();
    private final Parameter processingConfig = Configurer.get("processing.config");
    private final String generateIds = Configurer.get("ids.generate").get();
    //
    @Resource(name = "changesProducer")
    private JmsTemplate changesProducer;

    @PreDestroy
    public void destroy() {
        cassandraContentPersistenceModule().init();
        esContentIndexModule().init();
    }

    @Bean
    public ElasticSearchContentIndexModule esContentIndexModule() {
        return new ElasticSearchContentIndexModule(esSeeds, Long.parseLong(esRequestTimeout));
    }

    @Bean
    public MongoContentPersistenceModule mongoContentPersistenceModule() {
        return new MongoContentPersistenceModule(databasedMongo());
    }

    @Bean
    public CassandraContentPersistenceModule cassandraContentPersistenceModule() {
        CassandraContentPersistenceModule cassandraContentPersistenceModule = new CassandraContentPersistenceModule(cassandraEnv, cassandraSeeds, Integer.parseInt(cassandraPort), Integer.parseInt(cassandraConnectionTimeout), Integer.parseInt(cassandraRequestTimeout));
        cassandraContentPersistenceModule.init();
        return cassandraContentPersistenceModule;
    }

    @Bean
    public ContentBootstrapperModule contentBootstrapperModule() {
        return new ContentBootstrapperModule(cassandraContentPersistenceModule().cassandraContentStore());
    }
    
    @Bean
    public ContentBootstrapperModule contentBootstrapperModule() {
        return new ContentBootstrapperModule(contentLister(), cassandraContentPersistenceModule().cassandraContentStore());
    }

    @Bean
    public DatabasedMongo databasedMongo() {
        return new DatabasedMongo(mongo(), mongoDbName);
    }

    @Bean
    public Mongo mongo() {
        Mongo mongo = new Mongo(mongoHosts());
        if (processingConfig == null || !processingConfig.toBoolean()) {
            mongo.slaveOk();
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
    public MongoContentLister contentLister() {
        return mongoContentPersistenceModule().contentLister();
    }

    @Bean
    public MongoContentGroupWriter contentGroupWriter() {
        return mongoContentPersistenceModule().contentGroupWriter();
    }

    @Bean
    public MongoContentGroupResolver contentGroupResolver() {
        return mongoContentPersistenceModule().contentGroupResolver();
    }

    @Bean
    public ContentWriter contentWriter() {
        ContentWriter contentWriter = mongoContentPersistenceModule().contentWriter();
        contentWriter = new EquivalenceWritingContentWriter(contentWriter, lookupStore());
        if (Boolean.valueOf(generateIds)) {
            contentWriter = new IdSettingContentWriter(lookupStore(), idGeneratorBuilder().generator("content"), contentWriter);
        }
        contentWriter = new MessageQueueingContentWriter(changesProducer, contentWriter);
        return contentWriter;
    }

    @Bean
    public LookupResolvingContentResolver contentResolver() {
        return mongoContentPersistenceModule().contentResolver();
    }

    @Bean
    public QueuingItemsPeopleWriter itemsPeopleWriter() {
        return mongoContentPersistenceModule().itemsPeopleWriter();
    }

    @Bean
    public MongoContentResolver knownTypeContentResolver() {
        return mongoContentPersistenceModule().knownTypeContentResolver();
    }

    @Bean
    public TopicCreatingTopicResolver topicStore() {
        return mongoContentPersistenceModule().topicStore();
    }
    
    @Bean
    public TopicQueryResolver topicQueryResolver() {
        return mongoContentPersistenceModule().topicQueryResolver();
    }

    @Bean
    @Primary
    public MongoShortUrlSaver shortUrlSaver() {
        return mongoContentPersistenceModule().shortUrlSaver();
    }

    @Bean
    public IdSettingSegmentWriter segmentWriter() {
        return new IdSettingSegmentWriter(mongoContentPersistenceModule().segmentWriter(), segmentResolver(), idGeneratorBuilder().generator("segment"));
    }

    @Bean
    public MongoSegmentResolver segmentResolver() {
        return mongoContentPersistenceModule().segmentResolver();
    }

    @Bean
    public MongoProductStore productStore() {
        return mongoContentPersistenceModule().productStore();
    }

    @Bean
    public MongoLookupEntryStore lookupStore() {
        return mongoContentPersistenceModule().lookupStore();
    }

    @Bean
    public MongoChannelStore channelStore() {
        return mongoContentPersistenceModule().channelStore();
    }

    @Bean
    public MongoChannelGroupStore channelGroupStore() {
        return mongoContentPersistenceModule().channelGroupStore();
    }

    @Bean
    public MongoPersonStore personStore() {
        return mongoContentPersistenceModule().personStore();
    }

    @Bean
    public MongoScheduleStore scheduleStore() {
        return mongoContentPersistenceModule().scheduleStore();
    }

    @Bean
    public MongoMessageStore messageStore() {
        return mongoContentPersistenceModule().messageStore();
    }

    @Bean
    public ESContentIndexer contentIndexer() {
        return esContentIndexModule().contentIndexer();
    }

    @Bean
    public EsScheduleIndex scheduleIndex() {
        return esContentIndexModule().scheduleIndex();
    }

    @Bean
    public ESTopicSearcher topicSearcher() {
        return esContentIndexModule().topicSearcher();
    }
    
    @Bean
    @Primary
    public ESContentSearcher contentSearcher() {
        return esContentIndexModule().contentSearcher();
    }

    @Bean
    public ESContentSearcher contentSearcher() {
        return esContentIndexModule().contentSearcher();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraContentStore cassandraContentStore() {
        return cassandraContentPersistenceModule().cassandraContentStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraChannelGroupStore cassandraChannelGroupStore() {
        return cassandraContentPersistenceModule().cassandraChannelGroupStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraChannelStore cassandraChannelStore() {
        return cassandraContentPersistenceModule().cassandraChannelStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraContentGroupStore cassandraContentGroupStore() {
        return cassandraContentPersistenceModule().cassandraContentGroupStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraPersonStore cassandraPersonStore() {
        return cassandraContentPersistenceModule().cassandraPersonStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraProductStore cassandraProductStore() {
        return cassandraContentPersistenceModule().cassandraProductStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraSegmentStore cassandraSegmentStore() {
        return cassandraContentPersistenceModule().cassandraSegmentStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraTopicStore cassandraTopicStore() {
        return cassandraContentPersistenceModule().cassandraTopicStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraLookupEntryStore cassandraLookupEntryStore() {
        return cassandraContentPersistenceModule().cassandraLookupEntryStore();
    }

    @Bean
    @Primary
    @Qualifier(value = "cassandra")
    public CassandraEquivalenceSummaryStore cassandraEquivalenceSummaryStore() {
        return cassandraContentPersistenceModule().cassandraEquivalenceSummaryStore();
    }

    @Bean
    @Qualifier("cassandra")
    public ContentBootstrapper cassandraContentBootstrapper() {
        ContentBootstrapper bootstrapper = new ContentBootstrapper();
        bootstrapper.withChannelGroupListers(channelGroupStore());
        bootstrapper.withChannelListers(channelStore());
        bootstrapper.withContentGroupListers(contentGroupResolver());
        bootstrapper.withContentListers(contentLister());
        bootstrapper.withLookupEntryListers(lookupStore());
        bootstrapper.withPeopleListers(personStore());
        bootstrapper.withProductListers(productStore());
        bootstrapper.withSegmentListers(segmentResolver());
        bootstrapper.withTopicListers(topicStore());
        return bootstrapper;
    }

    @Bean
    @Qualifier("es")
    public ContentBootstrapper esContentBootstrapper() {
        ContentBootstrapper bootstrapper = new ContentBootstrapper();
        bootstrapper.withContentListers(contentLister(), cassandraContentStore());
        return bootstrapper;
    }

    @Bean
    MongoReplicaSetProbe mongoReplicaSetProbe() {
        return new MongoReplicaSetProbe(mongo());
    }

    @Bean
    MongoIOProbe mongoIoSetProbe() {
        return new MongoIOProbe(mongo()).withWriteConcern(WriteConcern.REPLICAS_SAFE);
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
}
