package org.atlasapi.remotesite.msnvideo;

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
public class MsnVideoModule {
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriters;
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(msnVideoAllShowsUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO)
            .withDescription("MSN Video update scheduled task installed")
            .withSource(MsnVideoAllShowsUpdater.class));
    }   
    
    public @Bean MsnVideoAllShowsUpdater msnVideoAllShowsUpdater() {
        return new MsnVideoAllShowsUpdater(HttpClients.screenScrapingClient(), log, msnVideoBrandAdapter(), contentWriters);
    }
    
    public @Bean MsnVideoItemAdapter msnVideoItemAdapter() {
        return new MsnVideoItemAdapter(HttpClients.screenScrapingClient(), log);
    }
    
    public @Bean MsnVideoBrandAdapter msnVideoBrandAdapter() {
        return new MsnVideoBrandAdapter(HttpClients.screenScrapingClient(), log, msnVideoItemAdapter());
    }
    
    public Collection<SiteSpecificAdapter<? extends Content>> adapters() {
        return ImmutableList.<SiteSpecificAdapter<? extends Content>>of(msnVideoBrandAdapter());
    }
}
