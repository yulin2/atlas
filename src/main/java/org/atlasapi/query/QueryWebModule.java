package org.atlasapi.query;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.feeds.www.DispatchingAtlasModelWriter;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.query.content.people.ContentResolvingPeopleResolver;
import org.atlasapi.query.content.schedule.BroadcastRemovingScheduleOverlapListener;
import org.atlasapi.query.content.schedule.ScheduleOverlapListener;
import org.atlasapi.query.content.schedule.ScheduleOverlapResolver;
import org.atlasapi.query.content.schedule.ThreadedScheduleOverlapListener;
import org.atlasapi.query.v2.PeopleController;
import org.atlasapi.query.v2.QueryController;
import org.atlasapi.query.v2.ScheduleController;
import org.atlasapi.query.v2.SearchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryWebModule {
    
    private @Autowired MongoDbBackedContentStore store;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired PeopleResolver peopleResolver;
    @Autowired
    private KnownTypeQueryExecutor queryExecutor;
    @Autowired
    private ApplicationConfigurationFetcher configFetcher;
    @Autowired
    private AdapterLog log;
    
    @Bean QueryController queryController() {
        return new QueryController(queryExecutor, configFetcher, log, atlasModelOutputter());
    }
    
    @Bean ScheduleOverlapListener scheduleOverlapListener() {
        BroadcastRemovingScheduleOverlapListener broadcastRemovingListener = new BroadcastRemovingScheduleOverlapListener(store, store);
        return new ThreadedScheduleOverlapListener(broadcastRemovingListener, log);
    }
    
    @Bean ScheduleController schedulerController() {
        ScheduleOverlapResolver resolver = new ScheduleOverlapResolver(scheduleResolver, scheduleOverlapListener(), log);
        return new ScheduleController(resolver, configFetcher, log, atlasModelOutputter());
    }
    
    @Bean PeopleController peopleController() {
        return new PeopleController(new ContentResolvingPeopleResolver(peopleResolver, queryExecutor), configFetcher, log, atlasModelOutputter());
    }
    
    @Bean SearchController searchController() {
        return new SearchController(searchResolver, configFetcher, log, atlasModelOutputter());
    }

    @Bean AtlasModelWriter atlasModelOutputter() {
        return new DispatchingAtlasModelWriter();
    }
}
