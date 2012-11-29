package org.atlasapi.remotesite.preview;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class PreviewNetworksModule {
    
    @Value("${preview.feedUrl}")
    private String feedUrl;
    
    @Autowired
    private ContentWriter contentWriter;
    
    @Autowired
    private AdapterLog log;
    
    @Autowired
    private SimpleScheduler scheduler;
    
    @Autowired
    private DatabasedMongo mongo;
    
    
    @Bean
    public PreviewFilmClipFeedUpdater previewFilmClipFeedUpdater() {
        PreviewFilmClipFeedUpdater previewFilmClipFeedUpdater = new PreviewFilmClipFeedUpdater(feedUrl, contentWriter, log, lastUpdatedStore());
        
        scheduler.schedule(previewFilmClipFeedUpdater.withName("Preview Networks Film Clip Updater"), RepetitionRules.daily(new LocalTime(5, 45)));
        
        return previewFilmClipFeedUpdater;
    }
    

    private PreviewLastUpdatedStore lastUpdatedStore() {
        return new MongoPreviewLastUpdatedStore(mongo);
    }
}
