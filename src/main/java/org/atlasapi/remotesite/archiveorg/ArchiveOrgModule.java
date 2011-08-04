package org.atlasapi.remotesite.archiveorg;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import({ArchiveOrgAdapterModule.class})
public class ArchiveOrgModule {
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter contentWriter;
    private @Autowired AdapterLog log;
    
    private @Autowired ArchiveOrgItemAdapter archiveOrgItemAdapter;
    
    private Iterable<String> playlists = ImmutableList.of("http://www.archive.org/search.php?query=collection:classic_tv", 
                                                          "http://www.archive.org/search.php?query=collection:feature_films");
    
//    @PostConstruct
//    public void startBackgroundTasks() {
//        scheduler.schedule(archiveOrgPlaylistUpdater(), AT_NIGHT);
//        log.record(new AdapterLogEntry(Severity.INFO).withDescription("Archive.org update scheduled task installed").withSource(ArchiveOrgPlaylistsUpdater.class));
//    } 
//    
//    public @Bean ArchiveOrgPlaylistsUpdater archiveOrgPlaylistUpdater() {
//        return new ArchiveOrgPlaylistsUpdater(HttpClients.screenScrapingClient(), archiveOrgItemAdapter, playlists, contentWriter, log);
//    }

}
