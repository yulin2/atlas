package org.atlasapi.remotesite.hbo;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;

@Configuration
public class HboModule {
    
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriter;
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(siteMapUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO)
            .withDescription("HBO update scheduled task installed")
            .withSource(HboSiteMapUpdater.class));
    } 
    
    public @Bean HboSiteMapUpdater siteMapUpdater() {
        return new HboSiteMapUpdater(HttpClients.screenScrapingClient(), hboBrandAdapter(), contentWriter, log);
    }
    
    public @Bean HboAdapterHelper hboAdapterHelper() {
        return new HboAdapterHelper();
    }
    
    public @Bean HboItemAdapter hboEpisodeAdapter() {
        return new HboItemAdapter(HttpClients.screenScrapingClient(), log, hboAdapterHelper());
    }
    
    public @Bean HboBrandAdapter hboBrandAdapter() {
        return new HboBrandAdapter(hboEpisodeAdapter(), HttpClients.screenScrapingClient(), log, hboAdapterHelper());
    }
    
    public Collection<SiteSpecificAdapter<? extends Content>> adapters() {
        return ImmutableList.<SiteSpecificAdapter<? extends Content>>of(hboBrandAdapter());
    }
}
