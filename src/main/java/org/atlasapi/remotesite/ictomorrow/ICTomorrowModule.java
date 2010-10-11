package org.atlasapi.remotesite.ictomorrow;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.archiveorg.ArchiveOrgItemAdapter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.security.UsernameAndPassword;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiHelper;

@Configuration
public class ICTomorrowModule {
    
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Value("${ict.username}") String ictUsername;
    private @Value("${ict.password}") String ictPassword;
    private @Autowired MongoDbBackedContentStore contentStore;
    private @Autowired AdapterLog log;
    
    
    @PostConstruct
    public void startBackgroundTasks() {
        if (!"DISABLED".equals(ictUsername)) {
            ictomorrowPlaylistUpdater().run();
            scheduler.schedule(ictomorrowPlaylistUpdater(), AT_NIGHT);
            log.record(new AdapterLogEntry(Severity.INFO)
                .withDescription("ICTomorrow update scheduled task installed")
                .withSource(ICTomorrowPlaylistUpdater.class));
        } else {
            log.record(new AdapterLogEntry(Severity.INFO)
                .withDescription("Not running ICTomorrow update because user/pass not present")
                .withSource(ICTomorrowPlaylistUpdater.class));
        }
    }
    
    public @Bean ArchiveOrgItemAdapter archiveOrgItemAdapter() {
        return new ArchiveOrgItemAdapter(HttpClients.webserviceClient());
    }

    public @Bean ICTomorrowPlaylistUpdater ictomorrowPlaylistUpdater() {
        return new ICTomorrowPlaylistUpdater(new ICTomorrowApiHelper(new UsernameAndPassword(ictUsername, ictPassword)), contentStore, archiveOrgItemAdapter());
    }
}
