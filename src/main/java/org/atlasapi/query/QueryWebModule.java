package org.atlasapi.query;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.feeds.www.DispatchingAtlasModelWriter;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
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
    
    private @Autowired ContentWriter contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired PeopleResolver peopleResolver;
    private @Autowired SegmentResolver segmentResolver;
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
        return new ScheduleController(resolver, configFetcher, log, atlasModelOutputter());
    }
    
    @Bean PeopleController peopleController() {
        return new PeopleController(peopleResolver, configFetcher, log, atlasModelOutputter());
    }
    
    @Bean SearchController searchController() {
        return new SearchController(searchResolver, configFetcher, log, atlasModelOutputter());
    }

    @Bean AtlasModelWriter atlasModelOutputter() {
        return new DispatchingAtlasModelWriter(contentResolver, segmentResolver);
    }
}
