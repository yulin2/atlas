package org.atlasapi.remotesite.five;

import javax.annotation.PostConstruct;

import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class FiveModule {
private final static Daily DAILY = RepetitionRules.daily(new LocalTime(4, 30, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter contentWriter;
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(fiveUpdater().withName("Five Updater"), DAILY);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed Five updater"));
    }
    
    @Bean
    public FiveUpdater fiveUpdater() {
        return new FiveUpdater(contentWriter, log);
    }
}
