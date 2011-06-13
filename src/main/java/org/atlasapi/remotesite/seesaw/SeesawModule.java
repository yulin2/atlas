//package org.atlasapi.remotesite.seesaw;
//
//import javax.annotation.PostConstruct;
//
//import org.atlasapi.persistence.logging.AdapterLog;
//import org.atlasapi.persistence.logging.AdapterLogEntry;
//import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
//import org.atlasapi.remotesite.ContentWriters;
//import org.joda.time.LocalTime;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.metabroadcast.common.scheduling.RepetitionRules;
//import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
//import com.metabroadcast.common.scheduling.SimpleScheduler;
//
//@Configuration
//public class SeesawModule {
//    
//    private final static Daily DAILY = RepetitionRules.daily(new LocalTime(4, 0, 0));
//    
//    private @Autowired SimpleScheduler scheduler;
//    private @Autowired ContentWriters contentWriter;
//    private @Autowired AdapterLog log;
//    
//    private @Value("${seesaw.feedUri}") String feedUri;
//    
//    @PostConstruct
//    public void startBackgroundTasks() {
//        if("DISABLED".equalsIgnoreCase(feedUri)) {
//            log.record(new AdapterLogEntry(Severity.WARN)
//            .withDescription("Feed URI required for Seesaw scheduled updater")
//            .withSource(SeesawRssUpdater.class));
//        }
//        scheduler.schedule(rssUpdater(), DAILY);
//        log.record(new AdapterLogEntry(Severity.INFO).withDescription("Seesaw update scheduled task installed").withSource(SeesawRssUpdater.class));
//    }
//    
//    public @Bean SeesawRssUpdater rssUpdater() {
//        return new SeesawRssUpdater(contentWriter, log, feedUri);
//    }
//
//}
