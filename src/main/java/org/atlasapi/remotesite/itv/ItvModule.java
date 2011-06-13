package org.atlasapi.remotesite.itv;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class ItvModule {
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriters;
    private @Autowired AdapterLog log;
    
    @PostConstruct 
    public void scheduleTasks() {
        scheduler.schedule(updater(), RepetitionRules.daily(new LocalTime(4, 0, 0)));
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("ITV scheduled updater installed"));
    }
    
    @Bean ItvUpdater updater() {
        return new ItvUpdater(itvBrandAdapter(), log);
    }
    
    @Bean ItvMercuryBrandAdapter itvBrandAdapter() {
        return new ItvMercuryBrandAdapter(contentWriters);
    }
    
    @Bean ItvMercuryEpisodeAdapter itvEpisodeAdapter() {
        return new ItvMercuryEpisodeAdapter();
    }
}
