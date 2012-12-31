package org.atlasapi.query;

import static org.atlasapi.output.Annotation.AVAILABLE_LOCATIONS;
import static org.atlasapi.output.Annotation.BRAND_REFERENCE;
import static org.atlasapi.output.Annotation.BRAND_SUMMARY;
import static org.atlasapi.output.Annotation.BROADCASTS;
import static org.atlasapi.output.Annotation.CHANNEL;
import static org.atlasapi.output.Annotation.CHANNELS;
import static org.atlasapi.output.Annotation.CHANNEL_SUMMARY;
import static org.atlasapi.output.Annotation.CLIPS;
import static org.atlasapi.output.Annotation.CONTENT_DETAIL;
import static org.atlasapi.output.Annotation.CONTENT_GROUPS;
import static org.atlasapi.output.Annotation.CONTENT_SUMMARY;
import static org.atlasapi.output.Annotation.DESCRIPTION;
import static org.atlasapi.output.Annotation.EXTENDED_DESCRIPTION;
import static org.atlasapi.output.Annotation.EXTENDED_ID;
import static org.atlasapi.output.Annotation.FILTERING_RESOURCE;
import static org.atlasapi.output.Annotation.FIRST_BROADCASTS;
import static org.atlasapi.output.Annotation.ID;
import static org.atlasapi.output.Annotation.ID_SUMMARY;
import static org.atlasapi.output.Annotation.KEY_PHRASES;
import static org.atlasapi.output.Annotation.LICENSE;
import static org.atlasapi.output.Annotation.LOCATIONS;
import static org.atlasapi.output.Annotation.NEXT_BROADCASTS;
import static org.atlasapi.output.Annotation.PEOPLE;
import static org.atlasapi.output.Annotation.PRODUCTS;
import static org.atlasapi.output.Annotation.PUBLISHER;
import static org.atlasapi.output.Annotation.RECENTLY_BROADCAST;
import static org.atlasapi.output.Annotation.RELATED_LINKS;
import static org.atlasapi.output.Annotation.SEGMENT_EVENTS;
import static org.atlasapi.output.Annotation.SERIES_REFERENCE;
import static org.atlasapi.output.Annotation.SERIES_SUMMARY;
import static org.atlasapi.output.Annotation.SUB_ITEMS;
import static org.atlasapi.output.Annotation.TOPICS;
import static org.atlasapi.output.Annotation.UPCOMING;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.input.BrandModelTransformer;
import org.atlasapi.input.DefaultGsonModelReader;
import org.atlasapi.input.DelegatingModelTransformer;
import org.atlasapi.input.ItemModelTransformer;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.simple.ChannelGroupQueryResult;
import org.atlasapi.media.entity.simple.ChannelQueryResult;
import org.atlasapi.media.entity.simple.ContentGroupQueryResult;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.PeopleQueryResult;
import org.atlasapi.media.entity.simple.ProductQueryResult;
import org.atlasapi.media.entity.simple.ScheduleQueryResult;
import org.atlasapi.media.entity.simple.ScheduleChannel;
import org.atlasapi.media.entity.simple.TopicQueryResult;
import org.atlasapi.media.product.Product;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AnnotationRegistry;
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
import org.atlasapi.output.annotation.AvailableLocationsAnnotation;
import org.atlasapi.output.annotation.BrandReferenceAnnotation;
import org.atlasapi.output.annotation.BrandSummaryAnnotation;
import org.atlasapi.output.annotation.BroadcastsAnnotation;
import org.atlasapi.output.annotation.ChannelAnnotation;
import org.atlasapi.output.annotation.ChannelSummaryWriter;
import org.atlasapi.output.annotation.ChannelsAnnotation;
import org.atlasapi.output.annotation.ClipsAnnotation;
import org.atlasapi.output.annotation.ContentGroupsAnnotation;
import org.atlasapi.output.annotation.DescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedDescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedIdentificationAnnotation;
import org.atlasapi.output.annotation.FilteringResourceAnnotation;
import org.atlasapi.output.annotation.FirstBroadcastAnnotation;
import org.atlasapi.output.annotation.IdentificationAnnotation;
import org.atlasapi.output.annotation.IdentificationSummaryAnnotation;
import org.atlasapi.output.annotation.KeyPhrasesAnnotation;
import org.atlasapi.output.annotation.LicenseWriter;
import org.atlasapi.output.annotation.LocationsAnnotation;
import org.atlasapi.output.annotation.NextBroadcastAnnotation;
import org.atlasapi.output.annotation.NullWriter;
import org.atlasapi.output.annotation.PeopleAnnotation;
import org.atlasapi.output.annotation.ProductsAnnotation;
import org.atlasapi.output.annotation.PublisherAnnotation;
import org.atlasapi.output.annotation.RecentlyBroadcastAnnotation;
import org.atlasapi.output.annotation.RelatedLinksAnnotation;
import org.atlasapi.output.annotation.SegmentEventsAnnotation;
import org.atlasapi.output.annotation.SeriesReferenceAnnotation;
import org.atlasapi.output.annotation.SeriesSummaryAnnotation;
import org.atlasapi.output.annotation.SubItemAnnotation;
import org.atlasapi.output.annotation.TopicsAnnotation;
import org.atlasapi.output.annotation.UpcomingAnnotation;
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
import org.atlasapi.output.simple.ItemModelSimplifier;
import org.atlasapi.output.simple.ProductModelSimplifier;
import org.atlasapi.output.simple.PublisherSimplifier;
import org.atlasapi.output.simple.TopicModelSimplifier;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.PeopleResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.content.schedule.ScheduleIndex;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.media.channel.ChannelGroupStore;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.persistence.media.product.ProductResolver;
import org.atlasapi.persistence.media.segment.SegmentResolver;
import org.atlasapi.persistence.output.AvailableChildrenResolver;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoAvailableChildrenResolver;
import org.atlasapi.persistence.output.MongoContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoRecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.MongoUpcomingChildrenResolver;
import org.atlasapi.persistence.output.RecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.query.content.schedule.ScheduleOverlapListener;
import org.atlasapi.query.topic.PublisherFilteringTopicContentLister;
import org.atlasapi.query.topic.PublisherFilteringTopicResolver;
import org.atlasapi.query.v2.ChannelController;
import org.atlasapi.query.v2.ChannelGroupController;
import org.atlasapi.query.v2.ContentGroupController;
import org.atlasapi.query.v2.ContentWriteController;
import org.atlasapi.query.v2.PeopleController;
import org.atlasapi.query.v2.ProductController;
import org.atlasapi.query.v2.QueryController;
import org.atlasapi.query.v2.SearchController;
import org.atlasapi.query.v2.TopicController;
import org.atlasapi.query.v4.schedule.IndexBackedScheduleQueryExecutor;
import org.atlasapi.query.v4.schedule.ScheduleIndexDebugController;
import org.atlasapi.query.v4.schedule.ScheduleQueryExecutor;
import org.atlasapi.query.v4.schedule.ScheduleQueryResultWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.SystemClock;

@Configuration
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
    private @Autowired @Qualifier("v2") SearchResolver v2SearchResolver;
    private @Autowired @Qualifier("v4") SearchResolver v4SearchResolver;
    private @Autowired PeopleResolver peopleResolver;
    private @Autowired TopicQueryResolver topicResolver;
    private @Autowired @Qualifier("topicStore") TopicStore topicStore;
    private @Autowired TopicContentLister topicContentLister;
    private @Autowired TopicSearcher topicSearcher;
    private @Autowired SegmentResolver segmentResolver;
    private @Autowired ProductResolver productResolver;

    private @Autowired KnownTypeQueryExecutor queryExecutor;
    private @Autowired EquivalentContentResolver equivalentContentResolver;
    private @Autowired ApplicationConfigurationFetcher configFetcher;
    private @Autowired AdapterLog log;
    
    private @Autowired ScheduleIndex scheduleIndex;
    
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
        return new ChannelSimplifier(new SubstitutionTableNumberCodec(), channelResolver, publisherSimplifier());
    }
    
    @Bean ChannelGroupSimplifier channelGroupSimplifier() {
        return new ChannelGroupSimplifier(new SubstitutionTableNumberCodec(), channelGroupResolver, publisherSimplifier());
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
    
    BrandModelTransformer brandTransformer() {
    	return new BrandModelTransformer(contentResolver, topicStore, new SystemClock());
    }
    
    ItemModelTransformer itemTransformer() {
    	return new ItemModelTransformer(contentResolver, topicStore, new SystemClock());
    }
    
    ContentWriteController contentWriteController() {
        return new ContentWriteController(configFetcher, contentResolver, contentWriter, new DefaultGsonModelReader(), new DelegatingModelTransformer(brandTransformer(), itemTransformer()));
    }

    @Bean
    ScheduleOverlapListener scheduleOverlapListener() {
        return new ScheduleOverlapListener() {

            @Override
            public void itemRemovedFromSchedule(Item item, Broadcast broadcast) {
            }
        };
//        BroadcastRemovingScheduleOverlapListener broadcastRemovingListener = new BroadcastRemovingScheduleOverlapListener(contentResolver, contentWriter);
//        return new ThreadedScheduleOverlapListener(broadcastRemovingListener, log);
    }
    
    @Bean
    org.atlasapi.query.v4.schedule.ScheduleController v4ScheduleController() {
        ScheduleQueryExecutor scheduleQueryExecutor = new IndexBackedScheduleQueryExecutor(scheduleIndex, equivalentContentResolver);
        return new org.atlasapi.query.v4.schedule.ScheduleController(scheduleQueryExecutor, channelResolver, configFetcher, new ScheduleQueryResultWriter(annotations()));
    }
    
    @Bean
    org.atlasapi.query.v4.topic.TopicController v4TopicController() {
        return new org.atlasapi.query.v4.topic.TopicController(topicResolver, topicSearcher, topicModelOutputter(), configFetcher);
    }

    @Bean
    PeopleController peopleController() {
        return new PeopleController(peopleResolver, configFetcher, log, personModelOutputter());
    }

    @Bean
    SearchController searchController() {
        return new SearchController(v2SearchResolver, configFetcher, log, contentModelOutputter());
    }
    
    @Bean
    org.atlasapi.query.v4.search.SearchController v4SearchController() {
        return new org.atlasapi.query.v4.search.SearchController(v4SearchResolver, configFetcher, log, contentModelOutputter());
    }

    @Bean
    TopicController topicController() {
        return new TopicController(new PublisherFilteringTopicResolver(topicResolver), new PublisherFilteringTopicContentLister(topicContentLister), configFetcher, log, topicModelOutputter(), queryController());
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
    AtlasModelWriter<QueryResult<Content, ? extends Identified>> contentModelOutputter() {
        return this.<QueryResult<Content, ? extends Identified>>standardWriter(
                new SimpleContentModelWriter(new JsonTranslator<ContentQueryResult>(), itemModelSimplifier(), containerSimplifier(), topicSimplifier(), productSimplifier()),
                new SimpleContentModelWriter(new JaxbXmlTranslator<ContentQueryResult>(), itemModelSimplifier(), containerSimplifier(), topicSimplifier(), productSimplifier()));
    }

    @Bean
    ContainerModelSimplifier containerSimplifier() {
        AvailableChildrenResolver availableChildren = new MongoAvailableChildrenResolver(mongo);
        UpcomingChildrenResolver upcomingChildren = new MongoUpcomingChildrenResolver(mongo);
        RecentlyBroadcastChildrenResolver recentChildren = new MongoRecentlyBroadcastChildrenResolver(mongo);
        ContainerModelSimplifier containerSimplier = new ContainerModelSimplifier(itemModelSimplifier(), localHostName, contentGroupResolver, topicResolver, availableChildren, upcomingChildren, productResolver, recentChildren);
        containerSimplier.exposeIds(Boolean.valueOf(exposeIds));
        return containerSimplier;
    }

    @Bean
    ItemModelSimplifier itemModelSimplifier() {
        NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
        ContainerSummaryResolver containerSummary = new MongoContainerSummaryResolver(mongo, idCodec);
        ItemModelSimplifier itemSimplifier = new ItemModelSimplifier(localHostName, contentGroupResolver, topicResolver, productResolver, segmentResolver, containerSummary, channelResolver, idCodec);
        itemSimplifier.exposeIds(Boolean.valueOf(exposeIds));
        return itemSimplifier;
    }

    @Bean
    AtlasModelWriter<Iterable<Person>> personModelOutputter() {
        return this.<Iterable<Person>>standardWriter(
                new SimplePersonModelWriter(new JsonTranslator<PeopleQueryResult>()),
                new SimplePersonModelWriter(new JaxbXmlTranslator<PeopleQueryResult>()));
    }

    @Bean
    AtlasModelWriter<ChannelSchedule> scheduleChannelModelOutputter() {
        return DispatchingAtlasModelWriter.<ChannelSchedule>dispatchingModelWriter()
            .register(new SimpleScheduleModelWriter(new JaxbXmlTranslator<ScheduleChannel>(), itemModelSimplifier(), channelSimplifier()), "xml", MimeType.APPLICATION_XML)
            .register(new SimpleScheduleModelWriter(new JsonTranslator<ScheduleChannel>(), itemModelSimplifier(), channelSimplifier()), "json", MimeType.APPLICATION_JSON)
            .build();
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
        ContentGroupModelSimplifier contentGroupModelSimplifier = new ContentGroupModelSimplifier();
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
        return DispatchingAtlasModelWriter.<I>dispatchingModelWriter()
            .register(new RdfXmlTranslator<I>(), "rdf.xml", MimeType.APPLICATION_RDF_XML)
            .register(jsonWriter, "json", MimeType.APPLICATION_JSON)
            .register(xmlWriter, "xml", MimeType.APPLICATION_XML)
            .build();
    }
    
    @Bean
    protected AnnotationRegistry annotations() {
        ImmutableSet<Annotation> commonImplied = ImmutableSet.of(ID_SUMMARY);
        SubstitutionTableNumberCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
        RecentlyBroadcastChildrenResolver recentlyBroadcastResolver = new MongoRecentlyBroadcastChildrenResolver(mongo);
        UpcomingChildrenResolver upcomingChildrenResolver = new MongoUpcomingChildrenResolver(mongo);
        MongoContainerSummaryResolver containerSummaryResolver = new MongoContainerSummaryResolver(mongo, idCodec);
        return AnnotationRegistry.builder()
        .register(ID_SUMMARY, new IdentificationSummaryAnnotation(idCodec))
        .register(ID, new IdentificationAnnotation(), commonImplied)
        .register(EXTENDED_ID, new ExtendedIdentificationAnnotation(idCodec), ImmutableSet.of(ID))
        .register(SERIES_REFERENCE, new SeriesReferenceAnnotation(), commonImplied)
        .register(SERIES_SUMMARY, new SeriesSummaryAnnotation(containerSummaryResolver), commonImplied, ImmutableSet.of(SERIES_REFERENCE))
        .register(BRAND_REFERENCE, new BrandReferenceAnnotation(), commonImplied)
        .register(BRAND_SUMMARY, new BrandSummaryAnnotation(containerSummaryResolver), commonImplied, ImmutableSet.of(BRAND_REFERENCE))
        .register(DESCRIPTION, new DescriptionAnnotation(), ImmutableSet.of(ID, SERIES_REFERENCE, BRAND_REFERENCE))
        .register(EXTENDED_DESCRIPTION, new ExtendedDescriptionAnnotation(), ImmutableSet.of(DESCRIPTION, EXTENDED_ID))
        .register(SUB_ITEMS, new SubItemAnnotation(), commonImplied)
        .register(CLIPS, new ClipsAnnotation(), commonImplied)
        .register(PEOPLE, new PeopleAnnotation(), commonImplied)
        .register(TOPICS, new TopicsAnnotation(topicResolver, localHostName, idCodec), commonImplied)
        .register(CONTENT_GROUPS, new ContentGroupsAnnotation(contentGroupResolver), commonImplied)
        .register(SEGMENT_EVENTS, new SegmentEventsAnnotation(), commonImplied)
        .register(RELATED_LINKS, new RelatedLinksAnnotation(), commonImplied)
        .register(KEY_PHRASES, new KeyPhrasesAnnotation(), commonImplied)
        .register(LOCATIONS, new LocationsAnnotation(), commonImplied)
        .register(BROADCASTS, new BroadcastsAnnotation(), commonImplied)
        .register(FIRST_BROADCASTS, new FirstBroadcastAnnotation(), commonImplied)
        .register(NEXT_BROADCASTS, new NextBroadcastAnnotation(new SystemClock()), commonImplied)
        .register(AVAILABLE_LOCATIONS, new AvailableLocationsAnnotation(), commonImplied)
        .register(UPCOMING, new UpcomingAnnotation(upcomingChildrenResolver), commonImplied)
        .register(FILTERING_RESOURCE, new FilteringResourceAnnotation(), commonImplied)
        .register(PRODUCTS, new ProductsAnnotation(productResolver), commonImplied)
        .register(RECENTLY_BROADCAST, new RecentlyBroadcastAnnotation(recentlyBroadcastResolver), commonImplied)
        .register(CHANNELS, new ChannelsAnnotation(), commonImplied)
        .register(PUBLISHER, new PublisherAnnotation(), commonImplied)
        .register(LICENSE, new LicenseWriter())
        .register(CHANNEL_SUMMARY, new ChannelSummaryWriter(), commonImplied)
        .register(CHANNEL, new ChannelAnnotation(), ImmutableSet.of(CHANNEL_SUMMARY))
        .register(CONTENT_SUMMARY, NullWriter.create(Content.class), ImmutableSet.of(DESCRIPTION, BRAND_SUMMARY, 
            SERIES_SUMMARY, BROADCASTS, LOCATIONS))
        .register(CONTENT_DETAIL, NullWriter.create(Content.class), ImmutableSet.of(EXTENDED_DESCRIPTION, SUB_ITEMS, CLIPS, 
            PEOPLE, BRAND_SUMMARY, SERIES_SUMMARY, BROADCASTS, LOCATIONS, KEY_PHRASES, RELATED_LINKS))
        .build();
    }
    
    @Bean
    public ScheduleIndexDebugController scheduleIndexDebug() {
        return new ScheduleIndexDebugController(scheduleIndex, channelResolver, configFetcher);
    }
}
