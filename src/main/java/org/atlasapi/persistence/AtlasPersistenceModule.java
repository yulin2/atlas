package org.atlasapi.persistence;

import java.net.UnknownHostException;
import java.util.List;

import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.IdGeneratorBuilder;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.health.MongoIOProbe;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;
import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.mongodb.Mongo;
import com.mongodb.MongoReplicaSetProbe;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import javax.annotation.Resource;
import org.atlasapi.media.content.util.EventQueueingContentWriter;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentIndexer;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.EquivalenceWritingContentWriter;
import org.atlasapi.persistence.content.IdSettingContentWriter;
import org.atlasapi.persistence.content.KnownTypeContentResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.mongo.LastUpdatedContentFinder;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.atlasapi.persistence.event.RecentChangeStore;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.persistence.media.product.IdSettingProductStore;
import org.atlasapi.persistence.media.product.ProductResolver;
import org.atlasapi.persistence.media.product.ProductStore;
import org.atlasapi.persistence.media.segment.IdSettingSegmentWriter;
import org.atlasapi.persistence.media.segment.SegmentResolver;
import org.atlasapi.persistence.media.segment.SegmentWriter;
import org.atlasapi.persistence.shorturls.ShortUrlSaver;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class AtlasPersistenceModule {

    private final String mongoHost = Configurer.get("mongo.host").get();
    private final String mongoDbName = Configurer.get("mongo.dbName").get();
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

    @Bean
    public ContentIndexModule esContentIndexModule() {
        ElasticSearchContentIndexModule elasticSearchContentIndexModule = new ElasticSearchContentIndexModule(esSeeds, Long.parseLong(esRequestTimeout));
        elasticSearchContentIndexModule.init();
        return elasticSearchContentIndexModule;
    }

    @Bean
    public MongoContentPersistenceModule mongoContentPersistenceModule() {
        return new MongoContentPersistenceModule(databasedMongo());
    }

    @Bean
    public CassandraContentPersistenceModule cassandraContentPersistenceModule() {
        return new CassandraContentPersistenceModule(cassandraSeeds, Integer.parseInt(cassandraPort), Integer.parseInt(cassandraConnectionTimeout), Integer.parseInt(cassandraRequestTimeout));
    }
    
    @Bean
    public CassandraContentStore cassandraContentStore() {
        return cassandraContentPersistenceModule().cassandraContentStore();
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
    public ContextConfigurer config() {
        ContextConfigurer c = new ContextConfigurer();
        c.init();
        return c;
    }

    @Bean
    @Primary
    public ContentGroupWriter contentGroupWriter() {
        return mongoContentPersistenceModule().contentGroupWriter();
    }

    @Bean
    @Primary
    public ContentGroupResolver contentGroupResolver() {
        return mongoContentPersistenceModule().contentGroupResolver();
    }

    @Bean
    @Primary
    public ContentWriter contentWriter() {
        ContentWriter contentWriter = mongoContentPersistenceModule().contentWriter();
        contentWriter = new EquivalenceWritingContentWriter(contentWriter, lookupStore());
        if (Boolean.valueOf(generateIds)) {
            contentWriter = new IdSettingContentWriter(lookupStore(), idGeneratorBuilder().generator("content"), contentWriter);
        }
        contentWriter = new EventQueueingContentWriter(changesProducer, contentWriter);
        return contentWriter;
    }

    @Bean
    @Primary
    public ItemsPeopleWriter itemsPeopleWriter() {
        return mongoContentPersistenceModule().itemsPeopleWriter();
    }

    @Bean
    @Primary
    public ContentResolver contentResolver() {
        return mongoContentPersistenceModule().contentResolver();
    }

    @Bean
    @Primary
    public TopicStore topicStore() {
        return mongoContentPersistenceModule().topicStore();
    }

    @Bean
    @Primary
    public TopicQueryResolver topicQueryResolver() {
        return mongoContentPersistenceModule().topicQueryResolver();
    }

    @Bean
    @Primary
    public ShortUrlSaver shortUrlSaver() {
        return mongoContentPersistenceModule().shortUrlSaver();
    }

    @Bean
    @Primary
    public SegmentWriter segmentWriter() {
        return new IdSettingSegmentWriter(mongoContentPersistenceModule().segmentWriter(), segmentResolver(), idGeneratorBuilder().generator("segment"));
    }

    @Bean
    @Primary
    public SegmentResolver segmentResolver() {
        return mongoContentPersistenceModule().segmentResolver();
    }

    @Bean
    @Primary
    public ProductStore productStore() {
        return new IdSettingProductStore(mongoContentPersistenceModule().productStore(), idGeneratorBuilder().generator("product"));
    }

    @Bean
    @Primary
    public ProductResolver productResolver() {
        return mongoContentPersistenceModule().productResolver();
    }

    @Bean
    @Primary
    public LookupEntryStore lookupStore() {
        return mongoContentPersistenceModule().lookupStore();
    }

    @Bean
    @Primary
    public ChannelResolver channelResolver() {
        return mongoContentPersistenceModule().channelResolver();
    }

    @Bean
    @Primary
    public ScheduleResolver scheduleResolver() {
        return mongoContentPersistenceModule().scheduleResolver();
    }

    @Bean
    @Primary
    public ScheduleWriter scheduleWriter() {
        return mongoContentPersistenceModule().scheduleWriter();
    }

    @Bean
    @Primary
    public KnownTypeContentResolver knownTypeContentResolver() {
        return mongoContentPersistenceModule().knownTypeContentResolver();
    }

    @Bean
    @Primary
    public LastUpdatedContentFinder lastUpdatedContentFinder() {
        return mongoContentPersistenceModule().lastUpdatedContentFinder();
    }

    @Bean
    @Primary
    public TopicContentLister topicContentLister() {
        return mongoContentPersistenceModule().topicContentLister();
    }

    @Bean
    @Primary
    public RecentChangeStore recentChangesStore() {
        return mongoContentPersistenceModule().recentChangesStore();
    }

    @Bean
    @Primary
    public ContentIndexer contentIndexer() {
        return esContentIndexModule().contentIndexer();
    }

    @Bean
    @Qualifier("cassandra")
    public ContentResolver cassandraContentResolver() {
        return cassandraContentPersistenceModule().contentResolver();
    }

    @Bean
    @Qualifier("cassandra")
    public ContentWriter cassandraContentWriter() {
        return cassandraContentPersistenceModule().contentWriter();
    }

    @Bean
    @Qualifier("cassandra")
    public ContentLister contentLister() {
        return cassandraContentPersistenceModule().contentLister();
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
