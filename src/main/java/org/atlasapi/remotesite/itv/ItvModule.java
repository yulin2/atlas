package org.atlasapi.remotesite.itv;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class ItvModule {
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriters contentWriters;
    private @Autowired AdapterLog log;
    
    @PostConstruct 
    public void scheduleTasks() {
        scheduler.schedule(updater(), RepetitionRules.daily(new LocalTime(4, 0, 0)));
    }
    
    @Bean ItvUpdater updater() {
        return new ItvUpdater(brandAdapter(), contentWriters, log);
    }
    
    @Bean ItvMercuryBrandAdapter brandAdapter() {
        return new ItvMercuryBrandAdapter();
    }
    
    @Bean ItvMercuryEpisodeAdapter episodeAdapter() {
        return new ItvMercuryEpisodeAdapter();
    }
    
    public Collection<SiteSpecificAdapter<? extends Content>> adapters() {
        return ImmutableList.<SiteSpecificAdapter<? extends Content>>of(episodeAdapter(), brandAdapter());
    }
}
