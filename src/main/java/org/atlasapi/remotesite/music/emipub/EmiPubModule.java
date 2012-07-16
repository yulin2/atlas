package org.atlasapi.remotesite.music.emipub;

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
public class EmiPubModule {

    private @Autowired @Qualifier(value="cassandra") ContentWriter contentWriter;
    private @Autowired SimpleScheduler scheduler;
    private @Autowired AdapterLog log;

    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(updater().withName("Emi Publishing Updater"), RepetitionRules.NEVER);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed Emi Publishing updater"));
    }

    @Bean(name="emipubupdater")
    public EmiPubUpdater updater() {
        return new EmiPubUpdater(contentWriter, log, Configurer.get("emipub.dataFile").get());
    }
}
