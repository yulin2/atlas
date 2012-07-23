package org.atlasapi.remotesite.space;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.RepetitionRule;
import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.joda.time.Duration;

@Configuration
public class TheSpaceModule {

    private final static RepetitionRule HOURLY = RepetitionRules.every(Duration.standardHours(1));
    private @Autowired
    SimpleScheduler scheduler;
    private @Autowired
    ContentResolver contentResolver;
    private @Autowired
    ContentWriter contentWriter;
    private @Autowired
    ContentGroupResolver groupResolver;
    private @Autowired
    ContentGroupWriter groupWriter;
    private @Autowired
    AdapterLog log;

    @PostConstruct
    public void startBackgroundTasks() throws Exception {
        scheduler.schedule(theSpaceUpdater().withName("TheSpace Updater"), HOURLY);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed TheSpace updater"));
    }

    @Bean
    public TheSpaceUpdater theSpaceUpdater() throws Exception {
        return new TheSpaceUpdater(contentResolver, contentWriter, groupResolver, groupWriter, log, Configurer.get("thespace.keystore.path").get(), Configurer.get("thespace.keystore.password").get(), Configurer.get("thespace.url").get());
    }
}
