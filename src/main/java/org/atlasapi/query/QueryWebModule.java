package org.atlasapi.query;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.feeds.utils.DescriptionWatermarker;
import org.atlasapi.feeds.utils.WatermarkModule;
import org.atlasapi.input.BrandModelTransformer;
import org.atlasapi.input.ClipModelTransformer;
import org.atlasapi.input.DefaultGsonModelReader;
import org.atlasapi.input.DelegatingModelTransformer;
import org.atlasapi.input.ItemModelTransformer;
import org.atlasapi.input.PersonModelTransformer;
import org.atlasapi.input.TopicModelTransformer;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.simple.ChannelGroupQueryResult;
import org.atlasapi.media.entity.simple.ChannelQueryResult;
import org.atlasapi.media.entity.simple.ContentGroupQueryResult;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.PeopleQueryResult;
import org.atlasapi.media.entity.simple.ProductQueryResult;
import org.atlasapi.media.entity.simple.ScheduleQueryResult;
import org.atlasapi.media.entity.simple.TopicQueryResult;
import org.atlasapi.media.product.Product;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.output.DispatchingAtlasModelWriter;
import org.atlasapi.output.JaxbXmlTranslator;
import org.atlasapi.output.JsonTranslator;
import org.atlasapi.output.QueryResult;
import org.atlasapi.output.SimpleChannelGroupModelWriter;
import org.atlasapi.output.SimpleChannelModelWriter;
import org.atlasapi.output.SimpleContentGroupModelWriter;
import org.atlasapi.output.SimpleContentModelWriter;
import org.atlasapi.output.SimplePersonModelWriter;
import org.atlasapi.output.SimpleProductModelWriter;
import org.atlasapi.output.SimpleScheduleModelWriter;
import org.atlasapi.output.SimpleTopicModelWriter;
import org.atlasapi.output.rdf.RdfXmlTranslator;
import org.atlasapi.output.simple.ChannelGroupModelSimplifier;
import org.atlasapi.output.simple.ChannelGroupSimplifier;
import org.atlasapi.output.simple.ChannelModelSimplifier;
import org.atlasapi.output.simple.ChannelNumberingChannelGroupModelSimplifier;
import org.atlasapi.output.simple.ChannelNumberingChannelModelSimplifier;
import org.atlasapi.output.simple.ChannelNumberingsChannelGroupToChannelModelSimplifier;
import org.atlasapi.output.simple.ChannelNumberingsChannelToChannelGroupModelSimplifier;
import org.atlasapi.output.simple.ChannelSimplifier;
import org.atlasapi.output.simple.ContainerModelSimplifier;
import org.atlasapi.output.simple.ContentGroupModelSimplifier;
import org.atlasapi.output.simple.ImageSimplifier;
import org.atlasapi.output.simple.ItemModelSimplifier;
import org.atlasapi.output.simple.PersonModelSimplifier;
import org.atlasapi.output.simple.ProductModelSimplifier;
import org.atlasapi.output.simple.PublisherSimplifier;
import org.atlasapi.output.simple.TopicModelSimplifier;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.PeopleQueryResolver;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.people.PersonStore;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoAvailableItemsResolver;
import org.atlasapi.persistence.output.MongoContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoRecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.MongoUpcomingItemsResolver;
import org.atlasapi.persistence.output.RecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.query.topic.PublisherFilteringTopicContentLister;
import org.atlasapi.query.topic.PublisherFilteringTopicResolver;
import org.atlasapi.query.v2.ChannelController;
import org.atlasapi.query.v2.ChannelGroupController;
import org.atlasapi.query.v2.ContentGroupController;
import org.atlasapi.query.v2.ContentWriteController;
import org.atlasapi.query.v2.PeopleController;
import org.atlasapi.query.v2.PeopleWriteController;
import org.atlasapi.query.v2.ProductController;
import org.atlasapi.query.v2.QueryController;
import org.atlasapi.query.v2.ScheduleController;
import org.atlasapi.query.v2.SearchController;
import org.atlasapi.query.v2.TopicController;
import org.atlasapi.query.v2.TopicWriteController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.SystemClock;

@Configuration
@Import( { WatermarkModule.class } )
public class QueryWebModule {
    
    private @Value("${local.host.name}") String localHostName;
    private @Value("${ids.expose}") String exposeIds;
    
    private @Autowired DatabasedMongo mongo;
    private @Autowired ContentGroupWriter contentGroupWriter;
    private @Autowired ContentGroupResolver contentGroupResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired ChannelGroupResolver channelGroupResolver;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired PeopleResolver peopleResolver;
    private @Autowired TopicQueryResolver topicResolver;
    private @Autowired @Qualifier("topicStore") TopicStore topicStore;
    private @Autowired TopicContentLister topicContentLister;
    private @Autowired SegmentResolver segmentResolver;
    private @Autowired ProductResolver productResolver;
    private @Autowired PeopleQueryResolver peopleQueryResolver;
    private @Autowired PersonStore personStore;
    private @Autowired LookupEntryStore lookupStore;
    private @Autowired DescriptionWatermarker descriptionWatermarker;

    private @Autowired KnownTypeQueryExecutor queryExecutor;
    private @Autowired ApplicationConfigurationFetcher configFetcher;
    private @Autowired AdapterLog log;
    
    @Bean ChannelController channelController() {
        return new ChannelController(configFetcher, log, channelModelWriter(), channelResolver, new SubstitutionTableNumberCodec());
    }

    @Bean AtlasModelWriter<Iterable<Channel>> channelModelWriter() {
        ChannelModelSimplifier channelModelSimplifier = channelModelSimplifier();
        return this.<Iterable<Channel>>standardWriter(
            new SimpleChannelModelWriter(new JsonTranslator<ChannelQueryResult>(), channelModelSimplifier),
            new SimpleChannelModelWriter(new JaxbXmlTranslator<ChannelQueryResult>(), channelModelSimplifier));
    }
    
    @Bean ChannelModelSimplifier channelModelSimplifier() {
        return new ChannelModelSimplifier(channelSimplifier(), new ChannelNumberingsChannelToChannelGroupModelSimplifier(
            channelGroupResolver, 
            new ChannelNumberingChannelGroupModelSimplifier(channelGroupSimplifier())));
    }
    
    @Bean ChannelSimplifier channelSimplifier() {
        return new ChannelSimplifier(v3ChannelCodec(), v4ChannelCodec(), channelResolver, publisherSimplifier(), imageSimplifier());
    }
    
    @Bean ChannelGroupSimplifier channelGroupSimplifier() {
        return new ChannelGroupSimplifier(new SubstitutionTableNumberCodec(), channelGroupResolver, publisherSimplifier());
    }
    
    @Bean
    ImageSimplifier imageSimplifier() {
        return new ImageSimplifier();
    }

    private SubstitutionTableNumberCodec v3ChannelCodec() {
        return new SubstitutionTableNumberCodec();
    }
    
    private SubstitutionTableNumberCodec v4ChannelCodec() {
        return SubstitutionTableNumberCodec.lowerCaseOnly();
    }

    @Bean
    ChannelGroupController channelGroupController() {
        NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
        return new ChannelGroupController(configFetcher, log, channelGroupModelWriter(), channelGroupResolver, idCodec);
    }

    @Bean AtlasModelWriter<Iterable<ChannelGroup>> channelGroupModelWriter() {
        ChannelGroupModelSimplifier channelGroupModelSimplifier = ChannelGroupModelSimplifier();
        return this.<Iterable<ChannelGroup>>standardWriter(
            new SimpleChannelGroupModelWriter(new JsonTranslator<ChannelGroupQueryResult>(), channelGroupModelSimplifier),
            new SimpleChannelGroupModelWriter(new JaxbXmlTranslator<ChannelGroupQueryResult>(), channelGroupModelSimplifier));
    }

    @Bean ChannelGroupModelSimplifier ChannelGroupModelSimplifier() {
        return new ChannelGroupModelSimplifier(channelGroupSimplifier(), new ChannelNumberingsChannelGroupToChannelModelSimplifier(
            channelResolver,
            new ChannelNumberingChannelModelSimplifier(channelSimplifier())));
    }
    
    @Bean PublisherSimplifier publisherSimplifier() {
        return new PublisherSimplifier();
    }

    @Bean
    QueryController queryController() {
        return new QueryController(queryExecutor, configFetcher, log, contentModelOutputter(), contentWriteController());
    }
    
    NumberToShortStringCodec idCodec() {
        return SubstitutionTableNumberCodec.lowerCaseOnly();
    }
    
    ClipModelTransformer clipTransformer() {
        return new ClipModelTransformer(lookupStore, topicStore, idCodec(), new SystemClock());
    }
    
    BrandModelTransformer brandTransformer() {
        return new BrandModelTransformer(lookupStore, topicStore, idCodec(), clipTransformer(), new SystemClock());
    }
    
    ItemModelTransformer itemTransformer() {
        return new ItemModelTransformer(lookupStore, topicStore, idCodec(), clipTransformer(), new SystemClock());
    }  
    
    ContentWriteController contentWriteController() {
        return new ContentWriteController(configFetcher, contentResolver, contentWriter, new DefaultGsonModelReader(), new DelegatingModelTransformer(brandTransformer(), itemTransformer()));
    }
    
    TopicWriteController topicWriteController() {
        return new TopicWriteController(configFetcher, topicStore, new DefaultGsonModelReader(), new TopicModelTransformer());
    }

    @Bean
    ScheduleController schedulerController() {
        return new ScheduleController(scheduleResolver, channelResolver, configFetcher, log, scheduleChannelModelOutputter());
    }

    @Bean
    PeopleController peopleController() {
        return new PeopleController(peopleQueryResolver, configFetcher, log, personModelOutputter(), peopleWriteController());
    }

    private PeopleWriteController peopleWriteController() {
        return new PeopleWriteController(configFetcher, personStore, new DefaultGsonModelReader(), new PersonModelTransformer(new SystemClock(), personStore));
    }

    @Bean
    SearchController searchController() {
        return new SearchController(searchResolver, configFetcher, log, contentModelOutputter());
    }

    @Bean
    TopicController topicController() {
        return new TopicController(new PublisherFilteringTopicResolver(topicResolver), new PublisherFilteringTopicContentLister(topicContentLister), configFetcher, log, topicModelOutputter(), queryController(), topicWriteController());
    }

    @Bean
    ProductController productController() {
        return new ProductController(productResolver, queryExecutor, configFetcher, log, productModelOutputter(), queryController());
    }

    @Bean
    ContentGroupController contentGroupController() {
        return new ContentGroupController(contentGroupResolver, queryExecutor, configFetcher, log, contentGroupOutputter(), queryController());
    }

    @Bean
    AtlasModelWriter<QueryResult<Identified, ? extends Identified>> contentModelOutputter() {
        return this.<QueryResult<Identified, ? extends Identified>>standardWriter(
                new SimpleContentModelWriter(new JsonTranslator<ContentQueryResult>(), contentItemModelSimplifier(), containerSimplifier(), topicSimplifier(), productSimplifier(), imageSimplifier(), personSimplifier()),
                new SimpleContentModelWriter(new JaxbXmlTranslator<ContentQueryResult>(), contentItemModelSimplifier(), containerSimplifier(), topicSimplifier(), productSimplifier(), imageSimplifier(), personSimplifier()));
    }

    @Bean
    ContainerModelSimplifier containerSimplifier() {
        RecentlyBroadcastChildrenResolver recentChildren = new MongoRecentlyBroadcastChildrenResolver(mongo);
        NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
        ContainerSummaryResolver containerSummary = new MongoContainerSummaryResolver(mongo, idCodec);
        ContainerModelSimplifier containerSimplier = new ContainerModelSimplifier(contentItemModelSimplifier(), localHostName, contentGroupResolver, topicResolver, availableItemsResolver(), upcomingItemsResolver(), productResolver, recentChildren, imageSimplifier(),peopleQueryResolver,containerSummary);
        containerSimplier.exposeIds(Boolean.valueOf(exposeIds));
        return containerSimplier;
    }

    @Bean
    PersonModelSimplifier personSimplifier() {
        return new PersonModelSimplifier(imageSimplifier(), upcomingItemsResolver(), availableItemsResolver());
    }
    
    @Bean
    MongoUpcomingItemsResolver upcomingItemsResolver() {
        return new MongoUpcomingItemsResolver(mongo);
    }

    @Bean
    MongoAvailableItemsResolver availableItemsResolver() {
        return new MongoAvailableItemsResolver(mongo, lookupStore);
    }

    @Bean
    ItemModelSimplifier contentItemModelSimplifier() {
        return itemModelSimplifier(false);
    }
    
    @Bean
    ItemModelSimplifier scheduleItemModelSimplifier() {
        return itemModelSimplifier(true);
    }
    
    private ItemModelSimplifier itemModelSimplifier(boolean withWatermark) {
        NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
        NumberToShortStringCodec channelIdCodec = new SubstitutionTableNumberCodec();
        ContainerSummaryResolver containerSummary = new MongoContainerSummaryResolver(mongo, idCodec);
        DescriptionWatermarker watermarker = withWatermark ? descriptionWatermarker : null;
        ItemModelSimplifier itemSimplifier = new ItemModelSimplifier(localHostName, contentGroupResolver, 
                topicResolver, productResolver, segmentResolver, containerSummary, channelResolver, 
                idCodec, channelIdCodec, imageSimplifier(),peopleQueryResolver, upcomingItemsResolver(), 
                availableItemsResolver(), watermarker);
        itemSimplifier.exposeIds(Boolean.valueOf(exposeIds));
        return itemSimplifier;
    }

    @Bean
    AtlasModelWriter<Iterable<Person>> personModelOutputter() {
        return this.<Iterable<Person>>standardWriter(
                new SimplePersonModelWriter(new JsonTranslator<PeopleQueryResult>(), imageSimplifier(), upcomingItemsResolver(), availableItemsResolver()),
                new SimplePersonModelWriter(new JaxbXmlTranslator<PeopleQueryResult>(), imageSimplifier(), upcomingItemsResolver(), availableItemsResolver()));
    }

    @Bean
    AtlasModelWriter<Iterable<ScheduleChannel>> scheduleChannelModelOutputter() {
        return this.<Iterable<ScheduleChannel>>standardWriter(
                new SimpleScheduleModelWriter(new JsonTranslator<ScheduleQueryResult>(), scheduleItemModelSimplifier(), channelSimplifier()),
                new SimpleScheduleModelWriter(new JaxbXmlTranslator<ScheduleQueryResult>(), scheduleItemModelSimplifier(), channelSimplifier()));
    }

    @Bean
    AtlasModelWriter<Iterable<Topic>> topicModelOutputter() {
        TopicModelSimplifier topicModelSimplifier = topicSimplifier();
        return this.<Iterable<Topic>>standardWriter(
                new SimpleTopicModelWriter(new JsonTranslator<TopicQueryResult>(), contentResolver, topicModelSimplifier),
                new SimpleTopicModelWriter(new JaxbXmlTranslator<TopicQueryResult>(), contentResolver, topicModelSimplifier));
    }

    @Bean
    AtlasModelWriter<Iterable<Product>> productModelOutputter() {
        ProductModelSimplifier modelSimplifier = productSimplifier();
        return this.<Iterable<Product>>standardWriter(
                new SimpleProductModelWriter(new JsonTranslator<ProductQueryResult>(), contentResolver, modelSimplifier),
                new SimpleProductModelWriter(new JaxbXmlTranslator<ProductQueryResult>(), contentResolver, modelSimplifier));
    }

    @Bean
    AtlasModelWriter<Iterable<ContentGroup>> contentGroupOutputter() {
        ContentGroupModelSimplifier modelSimplifier = contentGroupSimplifier();
        return this.<Iterable<ContentGroup>>standardWriter(
                new SimpleContentGroupModelWriter(new JsonTranslator<ContentGroupQueryResult>(), modelSimplifier),
                new SimpleContentGroupModelWriter(new JaxbXmlTranslator<ContentGroupQueryResult>(), modelSimplifier));
    }

    @Bean
    ContentGroupModelSimplifier contentGroupSimplifier() {
        ContentGroupModelSimplifier contentGroupModelSimplifier = new ContentGroupModelSimplifier(imageSimplifier());
        return contentGroupModelSimplifier;
    }

    @Bean
    TopicModelSimplifier topicSimplifier() {
        TopicModelSimplifier topicModelSimplifier = new TopicModelSimplifier(localHostName);
        return topicModelSimplifier;
    }

    @Bean
    ProductModelSimplifier productSimplifier() {
        ProductModelSimplifier productModelSimplifier = new ProductModelSimplifier(localHostName);
        return productModelSimplifier;
    }

    private <I extends Iterable<?>> AtlasModelWriter<I> standardWriter(AtlasModelWriter<I> jsonWriter, AtlasModelWriter<I> xmlWriter) {
        return DispatchingAtlasModelWriter.<I>dispatchingModelWriter().register(new RdfXmlTranslator<I>(), "rdf.xml", MimeType.APPLICATION_RDF_XML).register(jsonWriter, "json", MimeType.APPLICATION_JSON).register(xmlWriter, "xml", MimeType.APPLICATION_XML).build();
    }
}
