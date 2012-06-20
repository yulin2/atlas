package org.atlasapi.persistence;

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

import com.metabroadcast.common.ids.IdGeneratorBuilder;

@Configuration @Primary
public class AtlasPersistenceModule implements ContentPersistenceModule {

    @Autowired @Qualifier("base") private ContentPersistenceModule delegate;
    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private IdGeneratorBuilder idGeneratorBuilder;
    
    @Value("${ids.generate}") private String generateIds;

    public AtlasPersistenceModule() {}
    
    public AtlasPersistenceModule(ContentPersistenceModule delegate, JmsTemplate jmsTemplate, IdGeneratorBuilder idGeneratorBuilder) {
        this.delegate = delegate;
        this.jmsTemplate = jmsTemplate;
        this.idGeneratorBuilder = idGeneratorBuilder;
    }
    
    @Override @Bean
    public ContentGroupWriter contentGroupWriter() {
        return delegate.contentGroupWriter();
    }

    @Override @Bean
    public ContentGroupResolver contentGroupResolver() {
        return delegate.contentGroupResolver();
    }

    @Override @Bean
    public ContentWriter contentWriter() {
        ContentWriter contentWriter = delegate.contentWriter();
        contentWriter = new EquivalenceWritingContentWriter(contentWriter, lookupStore());
        if (Boolean.valueOf(generateIds)) {
            contentWriter = new IdSettingContentWriter(lookupStore(), idGeneratorBuilder.generator("content"), contentWriter);
        }
        contentWriter = new EventQueueingContentWriter(jmsTemplate, contentWriter);
        return contentWriter;
    }

    @Override @Bean
    public ItemsPeopleWriter itemsPeopleWriter() {
        return delegate.itemsPeopleWriter();
    }

    @Override @Bean @Primary
    public ContentResolver contentResolver() {
        return delegate.contentResolver();
    }

    @Override @Bean
    public TopicStore topicStore() {
        return delegate.topicStore();
    }

    @Override @Bean
    public TopicQueryResolver topicQueryResolver() {
        return delegate.topicQueryResolver();
    }

    @Override @Bean
    public ShortUrlSaver shortUrlSaver() {
        return delegate.shortUrlSaver();
    }

    @Override @Bean
    public SegmentWriter segmentWriter() {
        return new IdSettingSegmentWriter(delegate.segmentWriter(), segmentResolver(), idGeneratorBuilder.generator("segment"));
    }

    @Override @Bean
    public SegmentResolver segmentResolver() {
        return delegate.segmentResolver();
    }

    @Override @Bean
    public ProductStore productStore() {
        return new IdSettingProductStore(delegate.productStore(), idGeneratorBuilder.generator("product"));
    }

    @Override @Bean
    public ProductResolver productResolver() {
        return delegate.productResolver();
    }

    @Override @Bean
    public LookupEntryStore lookupStore() {
        return delegate.lookupStore();
    }

    @Override @Bean
    public ChannelResolver channelResolver() {
        return delegate.channelResolver();
    }

    @Override @Bean
    public ScheduleResolver scheduleResolver() {
        return delegate.scheduleResolver();
    }

    @Override @Bean
    public ScheduleWriter scheduleWriter() {
        return delegate.scheduleWriter();
    }

    @Override @Bean
    public KnownTypeContentResolver knownTypeContentResolver() {
        return delegate.knownTypeContentResolver();
    }

    @Override @Bean
    public LastUpdatedContentFinder lastUpdatedContentFinder() {
        return delegate.lastUpdatedContentFinder();
    }

    @Override @Bean
    public TopicContentLister topicContentLister() {
        return delegate.topicContentLister();
    }

}
