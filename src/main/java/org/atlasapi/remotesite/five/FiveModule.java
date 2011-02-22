package org.atlasapi.remotesite.five;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentWriters;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class FiveModule {
private final static Daily DAILY = RepetitionRules.daily(new LocalTime(4, 30, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriter;
    private @Autowired AdapterLog log;
    
    private @Value("${five.apiBaseUrl}") String apiBaseUrl;
    
    @PostConstruct
    public void startBackgroundTasks() {
        
        if (!apiBaseUrl.equalsIgnoreCase("disabled")) {
            scheduler.schedule(fiveUpdater(), DAILY);
        }
    }
    
    @Bean
    public FiveUpdater fiveUpdater() {
        return new FiveUpdater(contentWriter, log, apiBaseUrl);
    }
}
