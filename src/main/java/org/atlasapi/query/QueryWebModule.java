package org.atlasapi.query;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.channel.ChannelGroupStore;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.PeopleQueryResult;
import org.atlasapi.media.entity.simple.ScheduleQueryResult;
import org.atlasapi.media.entity.simple.TopicQueryResult;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.output.DispatchingAtlasModelWriter;
import org.atlasapi.output.JaxbXmlTranslator;
import org.atlasapi.output.JsonTranslator;
import org.atlasapi.output.SimpleContentModelWriter;
import org.atlasapi.output.SimplePersonModelWriter;
import org.atlasapi.output.SimpleScheduleModelWriter;
import org.atlasapi.output.SimpleTopicModelWriter;
import org.atlasapi.output.rdf.RdfXmlTranslator;
import org.atlasapi.output.simple.ContainerModelSimplifier;
import org.atlasapi.output.simple.ItemModelSimplifier;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.output.AvailableChildrenResolver;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoAvailableChildrenResolver;
import org.atlasapi.persistence.output.MongoContainerSummaryResolver;
import org.atlasapi.persistence.output.MongoUpcomingChildrenResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.query.content.schedule.ScheduleOverlapListener;
import org.atlasapi.query.content.schedule.ScheduleOverlapResolver;
import org.atlasapi.query.topic.PublisherFilteringTopicContentLister;
import org.atlasapi.query.topic.PublisherFilteringTopicResolver;
import org.atlasapi.query.v2.ChannelController;
import org.atlasapi.query.v2.ChannelGroupController;
import org.atlasapi.query.v2.ChannelSimplifier;
import org.atlasapi.query.v2.PeopleController;
import org.atlasapi.query.v2.QueryController;
import org.atlasapi.query.v2.ScheduleController;
import org.atlasapi.query.v2.SearchController;
import org.atlasapi.query.v2.TopicController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class QueryWebModule {
    
    private @Autowired DatabasedMongo mongo;
    private @Autowired ContentWriter contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired ChannelGroupStore channelGroupResolver;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired PeopleResolver peopleResolver;
    private @Autowired TopicQueryResolver topicResolver;
    private @Autowired TopicContentLister topicContentLister;
    private @Autowired SegmentResolver segmentResolver;

    @Autowired
    private KnownTypeQueryExecutor queryExecutor;
    @Autowired
    private ApplicationConfigurationFetcher configFetcher;
    @Autowired
    private AdapterLog log;
    
    @Bean ChannelController channelController() {
        NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
        return new ChannelController(channelResolver, idCodec , new ChannelSimplifier(idCodec, channelResolver, channelGroupResolver));
    }
    
    @Bean ChannelGroupController channelGroupController() {
        NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
        return new ChannelGroupController(channelGroupResolver, idCodec , new ChannelSimplifier(idCodec, channelResolver, channelGroupResolver));
    }
    
    @Bean QueryController queryController() {
        return new QueryController(queryExecutor, configFetcher, log, contentModelOutputter());
    }
    
    @Bean ScheduleOverlapListener scheduleOverlapListener() {
        return new ScheduleOverlapListener() {
            @Override
            public void itemRemovedFromSchedule(Item item, Broadcast broadcast) {
            }
        };
//        BroadcastRemovingScheduleOverlapListener broadcastRemovingListener = new BroadcastRemovingScheduleOverlapListener(contentResolver, contentWriter);
//        return new ThreadedScheduleOverlapListener(broadcastRemovingListener, log);
    }
    
    @Bean ScheduleController schedulerController() {
        ScheduleOverlapResolver resolver = new ScheduleOverlapResolver(scheduleResolver, scheduleOverlapListener(), log);
        return new ScheduleController(resolver, channelResolver, configFetcher, log, scheduleChannelModelOutputter());
    }
    
    @Bean PeopleController peopleController() {
        return new PeopleController(peopleResolver, configFetcher, log, personModelOutputter());
    }
    
    @Bean SearchController searchController() {
        return new SearchController(searchResolver, configFetcher, log, contentModelOutputter());
    }
    
    @Bean TopicController topicController() {
        return new TopicController(new PublisherFilteringTopicResolver(topicResolver), new PublisherFilteringTopicContentLister(topicContentLister), configFetcher, log, topicModelOutputter(),queryController());
    }

    @Bean AtlasModelWriter<Iterable<Content>> contentModelOutputter() {
        return this.<Content>standardWriter(
            new SimpleContentModelWriter(new JsonTranslator<ContentQueryResult>(), itemModelSimplifier(), containerSimplifier()),
            new SimpleContentModelWriter(new JaxbXmlTranslator<ContentQueryResult>(), itemModelSimplifier(), containerSimplifier())
        );
    }

    @Bean ContainerModelSimplifier containerSimplifier() {
        AvailableChildrenResolver availableChildren = new MongoAvailableChildrenResolver(mongo);
        UpcomingChildrenResolver upcomingChildren = new MongoUpcomingChildrenResolver(mongo);
        ContainerModelSimplifier containerSimplier = new ContainerModelSimplifier(itemModelSimplifier(), topicResolver, availableChildren, upcomingChildren);
        return containerSimplier;
    }

    @Bean ItemModelSimplifier itemModelSimplifier() {
        ContainerSummaryResolver containerSummary = new MongoContainerSummaryResolver(mongo);
        ItemModelSimplifier itemSimplifier = new ItemModelSimplifier(topicResolver, segmentResolver, containerSummary);
        return itemSimplifier;
    }
    
    @Bean AtlasModelWriter<Iterable<Person>> personModelOutputter() {
        return this.<Person>standardWriter(
            new SimplePersonModelWriter(new JsonTranslator<PeopleQueryResult>()),
            new SimplePersonModelWriter(new JaxbXmlTranslator<PeopleQueryResult>())
        );
    }
    
    @Bean AtlasModelWriter<Iterable<ScheduleChannel>> scheduleChannelModelOutputter() {
        return this.<ScheduleChannel>standardWriter(
            new SimpleScheduleModelWriter(new JsonTranslator<ScheduleQueryResult>(), itemModelSimplifier()),
            new SimpleScheduleModelWriter(new JaxbXmlTranslator<ScheduleQueryResult>(), itemModelSimplifier())
        );
    }
    
    @Bean AtlasModelWriter<Iterable<Topic>> topicModelOutputter() {
        return this.<Topic>standardWriter(
            new SimpleTopicModelWriter(new JsonTranslator<TopicQueryResult>(), contentResolver),
            new SimpleTopicModelWriter(new JaxbXmlTranslator<TopicQueryResult>(),contentResolver)
        );
    }
    
    private <T> AtlasModelWriter<Iterable<T>> standardWriter(AtlasModelWriter<Iterable<T>> jsonWriter, AtlasModelWriter<Iterable<T>> xmlWriter) {
        return DispatchingAtlasModelWriter.<Iterable<T>>dispatchingModelWriter()
                .register(new RdfXmlTranslator<T>(), "rdf.xml", MimeType.APPLICATION_RDF_XML)
                .register(jsonWriter, "json", MimeType.APPLICATION_JSON)
                .register(xmlWriter, "xml", MimeType.APPLICATION_XML)
                .build();
    }
}
