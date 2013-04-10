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
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.schedule.ScheduleIndex;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.media.topic.PopularTopicIndex;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicResolver;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AnnotationRegistry;
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
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.output.MongoContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoRecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.MongoUpcomingChildrenResolver;
import org.atlasapi.persistence.output.RecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.query.common.AttributeCoercers;
import org.atlasapi.query.common.ContextualQueryParser;
import org.atlasapi.query.common.QueryAtomParser;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.QueryContextParser;
import org.atlasapi.query.common.QueryParameterAnnotationsExtractor;
import org.atlasapi.query.common.StandardQueryParser;
import org.atlasapi.query.v4.schedule.ScheduleController;
import org.atlasapi.query.v4.schedule.ScheduleIndexDebugController;
import org.atlasapi.query.v4.schedule.ScheduleQueryResultWriter;
import org.atlasapi.query.v4.search.ContentQueryResultWriter;
import org.atlasapi.query.v4.search.SearchController;
import org.atlasapi.query.v4.topic.PopularTopicController;
import org.atlasapi.query.v4.topic.TopicContentController;
import org.atlasapi.query.v4.topic.TopicContentResultWriter;
import org.atlasapi.query.v4.topic.TopicController;
import org.atlasapi.query.v4.topic.TopicQueryResultWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.time.SystemClock;

@Configuration
public class QueryWebModule {
    
    private @Value("${local.host.name}") String localHostName;
    private @Value("${ids.expose}") String exposeIds;
    
    private @Autowired DatabasedMongo mongo;
    private @Autowired QueryModule queryModule;
//    private @Autowired ContentGroupResolver contentGroupResolver;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired SearchResolver v4SearchResolver;
    private @Autowired TopicResolver topicResolver;
    private @Autowired PopularTopicIndex popularTopicIndex;
//    private @Autowired SegmentResolver segmentResolver;
//    private @Autowired ProductResolver productResolver;
    private @Autowired ScheduleIndex scheduleIndex;

    private @Autowired ApplicationConfigurationFetcher configFetcher;

    @Bean NumberToShortStringCodec idCodec() {
        return SubstitutionTableNumberCodec.lowerCaseOnly();
    }

    @Bean SelectionBuilder  selectionBuilder() {
        return Selection.builder().withDefaultLimit(50).withMaxLimit(100);
    }

    @Bean
    ScheduleController v4ScheduleController() {
        return new ScheduleController(
                queryModule.scheduleQueryExecutor(),
                channelResolver,
                configFetcher,
                new ScheduleQueryResultWriter(annotations()));
    }

    @Bean
    TopicController v4TopicController() {
        return new TopicController(topicQueryParser(), 
            queryModule.topicQueryExecutor(), new TopicQueryResultWriter(annotations()));
    }

    @Bean
    TopicContentController topicContentController() {
        QueryContextParser contextParser = new QueryContextParser(configFetcher,
            new QueryParameterAnnotationsExtractor(), selectionBuilder());
        
        ContextualQueryParser<Topic, Content> parser = new ContextualQueryParser<Topic, Content>(
            "topics", Attributes.TOPIC_ID, "content", idCodec(),
            contentQueryAttributeParser().copyWithIgnoredParameters(contextParser.getParameterNames()),
            contextParser);
        
        return new TopicContentController(parser, queryModule.topicContentQueryExecutor(),
                new TopicContentResultWriter(annotations()));
    }

    private QueryAttributeParser contentQueryAttributeParser() {
        return new QueryAttributeParser(ImmutableList.of(
            QueryAtomParser.valueOf(Attributes.ID, AttributeCoercers.idCoercer(idCodec())),
            QueryAtomParser.valueOf(Attributes.ALIASES_NAMESPACE, AttributeCoercers.stringCoercer()),
            QueryAtomParser.valueOf(Attributes.ALIASES_VALUE, AttributeCoercers.stringCoercer()),
            QueryAtomParser.valueOf(Attributes.TOPIC_RELATIONSHIP, AttributeCoercers.stringCoercer()),
            QueryAtomParser.valueOf(Attributes.TOPIC_SUPERVISED, AttributeCoercers.booleanCoercer()),
            QueryAtomParser.valueOf(Attributes.TOPIC_WEIGHTING, AttributeCoercers.floatCoercer())
        ));
    }

    private StandardQueryParser<Topic> topicQueryParser() {
        return new StandardQueryParser<Topic>("topics", 
            new QueryAttributeParser(ImmutableList.of(
                QueryAtomParser.valueOf(Attributes.ID, AttributeCoercers.idCoercer(idCodec())),
                QueryAtomParser.valueOf(Attributes.ALIASES_NAMESPACE, AttributeCoercers.stringCoercer()),
                QueryAtomParser.valueOf(Attributes.ALIASES_VALUE, AttributeCoercers.stringCoercer())
            )),
            idCodec(), new QueryContextParser(configFetcher, 
            new QueryParameterAnnotationsExtractor("topic"), selectionBuilder())
        );
    }

    @Bean
    PopularTopicController popularTopicController() {
        return new PopularTopicController(topicResolver, popularTopicIndex, new TopicQueryResultWriter(annotations()), configFetcher);
    }

    @Bean
    SearchController searchController() {
        return new SearchController(v4SearchResolver, configFetcher, new ContentQueryResultWriter(annotations()));
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
        //.register(CONTENT_GROUPS, new ContentGroupsAnnotation(contentGroupResolver), commonImplied)
        //.register(SEGMENT_EVENTS, new SegmentEventsAnnotation(segmentResolver), commonImplied)
        .register(RELATED_LINKS, new RelatedLinksAnnotation(), commonImplied)
        .register(KEY_PHRASES, new KeyPhrasesAnnotation(), commonImplied)
        .register(LOCATIONS, new LocationsAnnotation(), commonImplied)
        .register(BROADCASTS, new BroadcastsAnnotation(), commonImplied)
        .register(FIRST_BROADCASTS, new FirstBroadcastAnnotation(), commonImplied)
        .register(NEXT_BROADCASTS, new NextBroadcastAnnotation(new SystemClock()), commonImplied)
        .register(AVAILABLE_LOCATIONS, new AvailableLocationsAnnotation(), commonImplied)
        .register(UPCOMING, new UpcomingAnnotation(upcomingChildrenResolver), commonImplied)
        .register(FILTERING_RESOURCE, new FilteringResourceAnnotation(), commonImplied)
        //.register(PRODUCTS, new ProductsAnnotation(productResolver), commonImplied)
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
