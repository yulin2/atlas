package org.atlasapi.persistence;

import com.metabroadcast.common.ids.IdGeneratorBuilder;
import javax.annotation.Resource;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.content.util.EventQueueingContentWriter;
import org.atlasapi.media.product.IdSettingProductStore;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.media.product.ProductStore;
import org.atlasapi.media.segment.IdSettingSegmentWriter;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.media.segment.SegmentWriter;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.EquivalenceWritingContentWriter;
import org.atlasapi.persistence.content.IdSettingContentWriter;
import org.atlasapi.persistence.content.KnownTypeContentResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.mongo.LastUpdatedContentFinder;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.shorturls.ShortUrlSaver;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;
import org.atlasapi.persistence.event.RecentChangeStore;

@Configuration
@Primary
public class AtlasPersistenceModule {

    @Autowired
    @Qualifier("base")
    private ContentPersistenceModule delegate;
    @Autowired
    private IdGeneratorBuilder idGeneratorBuilder;
    @Resource(name = "changesProducer")
    private JmsTemplate changesProducer;
    @Value("${ids.generate}")
    private String generateIds;

    public AtlasPersistenceModule() {
    }

    public AtlasPersistenceModule(ContentPersistenceModule delegate, JmsTemplate changesProducer, IdGeneratorBuilder idGeneratorBuilder) {
        this.delegate = delegate;
        this.changesProducer = changesProducer;
        this.idGeneratorBuilder = idGeneratorBuilder;
    }

    @Bean
    public ContentGroupWriter contentGroupWriter() {
        return delegate.contentGroupWriter();
    }

    @Bean
    public ContentGroupResolver contentGroupResolver() {
        return delegate.contentGroupResolver();
    }

    @Bean
    public ContentWriter contentWriter() {
        ContentWriter contentWriter = delegate.contentWriter();
        contentWriter = new EquivalenceWritingContentWriter(contentWriter, lookupStore());
        if (Boolean.valueOf(generateIds)) {
            contentWriter = new IdSettingContentWriter(lookupStore(), idGeneratorBuilder.generator("content"), contentWriter);
        }
        contentWriter = new EventQueueingContentWriter(changesProducer, contentWriter);
        return contentWriter;
    }

    @Bean
    public ItemsPeopleWriter itemsPeopleWriter() {
        return delegate.itemsPeopleWriter();
    }

    @Bean
    @Primary
    public ContentResolver contentResolver() {
        return delegate.contentResolver();
    }

    @Bean
    public TopicStore topicStore() {
        return delegate.topicStore();
    }

    @Bean
    public TopicQueryResolver topicQueryResolver() {
        return delegate.topicQueryResolver();
    }

    @Bean
    public ShortUrlSaver shortUrlSaver() {
        return delegate.shortUrlSaver();
    }

    @Bean
    public SegmentWriter segmentWriter() {
        return new IdSettingSegmentWriter(delegate.segmentWriter(), segmentResolver(), idGeneratorBuilder.generator("segment"));
    }

    @Bean
    public SegmentResolver segmentResolver() {
        return delegate.segmentResolver();
    }

    @Bean
    public ProductStore productStore() {
        return new IdSettingProductStore(delegate.productStore(), idGeneratorBuilder.generator("product"));
    }

    @Bean
    public ProductResolver productResolver() {
        return delegate.productResolver();
    }

    @Bean
    public LookupEntryStore lookupStore() {
        return delegate.lookupStore();
    }

    @Bean
    public ChannelResolver channelResolver() {
        return delegate.channelResolver();
    }

    @Bean
    public ScheduleResolver scheduleResolver() {
        return delegate.scheduleResolver();
    }

    @Bean
    public ScheduleWriter scheduleWriter() {
        return delegate.scheduleWriter();
    }

    @Bean
    public KnownTypeContentResolver knownTypeContentResolver() {
        return delegate.knownTypeContentResolver();
    }

    @Bean
    @Primary
    public LastUpdatedContentFinder lastUpdatedContentFinder() {
        return delegate.lastUpdatedContentFinder();
    }

    @Bean
    public TopicContentLister topicContentLister() {
        return delegate.topicContentLister();
    }

    @Bean
    public RecentChangeStore recentChangesStore() {
        return delegate.recentChangesStore();
    }
}
