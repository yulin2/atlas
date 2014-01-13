package org.atlasapi.remotesite.metabroadcast.similar;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.MongoContentPersistenceModule;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import({ MongoContentPersistenceModule.class })
public class SimilarContentModule {

    private static RepetitionRule SIMILAR_CONTENT_UPDATER_REPETITION = RepetitionRules.daily(LocalTime.parse("15:00"));
    
    @Autowired 
    ContentLister contentLister;
    
    @Autowired
    SimpleScheduler scheduler;
    
    @Value("${updaters.similarcontent.enabled}") 
    Boolean tasksEnabled;
    
    @PostConstruct
    public void scheduleTasks() {
        if (Boolean.TRUE.equals(tasksEnabled)) {
            scheduler.schedule(similarContentUpdater(), SIMILAR_CONTENT_UPDATER_REPETITION);
        }
    }
    
    @Bean
    public SimilarContentUpdater similarContentUpdater() {
        return new SimilarContentUpdater(contentLister, Publisher.PA, similarContentProvider());
    }
    
    SimilarContentProvider similarContentProvider() {
        return new DefaultSimilarContentProvider(contentLister, Publisher.PA, 10, new GenreAndPeopleTraitHashCalculator());
    }
    
}
