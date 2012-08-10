package org.atlasapi.remotesite.bbckiwisubtitles;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.topic.TopicStore;
import org.jets3t.service.security.AWSCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class KiwiSubtitlesModule {
    
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    private @Autowired TopicStore topicStore;
    private @Autowired SimpleScheduler scheduler;

    private @Value("${s3.access}") String s3accesKey;
    private @Value("${s3.secret}") String s3secret;
    private @Value("${kiwi.subtitles.bucket}") String bucket;

    @Bean
    public KiwiSubtitlesTopicsUpdater kiwiSubtitlesTopicsUpdater() {
        return new KiwiSubtitlesTopicsUpdater(kiwiSubtitlesTopicsClient(), contentResolver, contentWriter, ancilliaryContentFactory(), topicStore);
    }
    
    @Bean 
    public S3KiwiSubtitlesTopicsClient kiwiSubtitlesTopicsClient() {
        return new S3KiwiSubtitlesTopicsClient(new AWSCredentials(s3accesKey, s3secret), bucket);
    }
    
    @Bean
    public AncilliaryContentFactory ancilliaryContentFactory() {
        return new AncilliaryContentFactory();
    }
    
    @PostConstruct
    public void schedule() {
        scheduler.schedule(kiwiSubtitlesTopicsUpdater().withName("Kiwi Subtitles Updater"), RepetitionRules.NEVER);
    }
}
