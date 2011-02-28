package org.atlasapi.remotesite.hbo;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.HttpClients;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import({HboAdapterModule.class})
public class HboModule {
    
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriter;
    private @Autowired AdapterLog log;
    
    private @Autowired HboBrandAdapter hboBrandAdapter;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(siteMapUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO).withDescription("HBO update scheduled task installed").withSource(HboSiteMapUpdater.class));
    }
    
    public @Bean HboSiteMapUpdater siteMapUpdater() {
        return new HboSiteMapUpdater(HttpClients.screenScrapingClient(), hboBrandAdapter, contentWriter, log);
    }
}
