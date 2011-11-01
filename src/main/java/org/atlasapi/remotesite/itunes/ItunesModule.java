package org.atlasapi.remotesite.itunes;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.HttpClients;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import({ItunesAdapterModule.class})
public class ItunesModule {
    
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriter;
    private @Autowired AdapterLog log;
    
    private @Autowired ItunesBrandAdapter itunesBrandAdapter;
    
    private Set<String> feeds = ImmutableSet.of("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toptvseasons/sf=143444/limit=300/xml",
                                                "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toptvepisodes/sf=143444/limit=300/xml");
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(itunesRssUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO).withDescription("iTunes update scheduled task installed").withSource(ItunesRssUpdater.class));
    } 
    
    public @Bean ItunesRssUpdater itunesRssUpdater() {
        return new ItunesRssUpdater(feeds, HttpClients.webserviceClient(), contentWriter, itunesBrandAdapter, log);
    }
    
}
