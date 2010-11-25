package org.atlasapi.remotesite.archiveorg;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;

@Configuration
public class ArchiveOrgModule {
    private final static Daily AT_NIGHT = RepetitionRules.daily(new LocalTime(5, 0, 0));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriter;
    private @Autowired AdapterLog log;
    
    private Iterable<String> playlists = ImmutableList.of("http://www.archive.org/search.php?query=collection:classic_tv", 
                                                          "http://www.archive.org/search.php?query=collection:feature_films");
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(archiveOrgPlaylistUpdater(), AT_NIGHT);
        log.record(new AdapterLogEntry(Severity.INFO)
            .withDescription("Archive.org update scheduled task installed")
            .withSource(ArchiveOrgPlaylistsUpdater.class));
    } 
    
    public @Bean ArchiveOrgItemAdapter archiveOrgItemAdapter() {
        return new ArchiveOrgItemAdapter(HttpClients.webserviceClient(), log);
    }
    
    public @Bean ArchiveOrgPlaylistsUpdater archiveOrgPlaylistUpdater() {
        return new ArchiveOrgPlaylistsUpdater(HttpClients.screenScrapingClient(), archiveOrgItemAdapter(), playlists, contentWriter, log);
    }
    
    public Collection<SiteSpecificAdapter<? extends Content>> adapters() {
        return ImmutableList.<SiteSpecificAdapter<? extends Content>>of(archiveOrgItemAdapter());
    }
}
