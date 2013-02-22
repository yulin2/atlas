package org.atlasapi.remotesite.bbc.products;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.media.product.ProductStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BbcProductsModule {

    private @Autowired SimpleScheduler scheduler;
    private @Autowired ProductStore productStore;
    private @Autowired AdapterLog log;

    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(updater().withName("BBC Products Updater"), RepetitionRules.NEVER);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed BBC Products updater"));
    }

    @Bean(name="bbcproductsupdater")
    public BbcProductsUpdater updater() {
        return new BbcProductsUpdater(productStore, log, Configurer.get("s3.access").get(), Configurer.get("s3.secret").get());
    }
}
