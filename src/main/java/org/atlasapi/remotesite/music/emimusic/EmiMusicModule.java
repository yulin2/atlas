package org.atlasapi.remotesite.music.emimusic;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.RepetitionRules;
import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.SimpleScheduler;
import org.atlasapi.persistence.content.ContentWriter;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class EmiMusicModule {

    private @Autowired @Qualifier(value="cassandra") ContentWriter contentWriter;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired AdapterLog log;

    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(updater().withName("EMI Music Updater"), RepetitionRules.NEVER);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed EMI Music Updater"));
    }

    @Bean(name="emimusicupdater")
    public EmiMusicUpdater updater() {
        return new EmiMusicUpdater(contentWriter, log, Configurer.get("s3.access").get(), Configurer.get("s3.secret").get());
    }
}
