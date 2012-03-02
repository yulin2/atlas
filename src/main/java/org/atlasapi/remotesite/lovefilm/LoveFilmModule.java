package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.remotesite.five.*;
import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentWriter;
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
public class LoveFilmModule {
private final static Daily DAILY = RepetitionRules.daily(new LocalTime(4, 30, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter contentWriter;
    private @Autowired AdapterLog log;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(updater().withName("LoveFilm Updater"), DAILY);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed LoveFilm updater"));
    }
    
    @Bean
    public LoveFilmUpdater updater() {
        return new LoveFilmUpdater(contentWriter, log, "ubdxxqxe4nrnbdae8wt92pqe", "v7upKdKxCe");
    }
}
