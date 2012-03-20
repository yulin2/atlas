package org.atlasapi.remotesite.worldservice;

import static org.atlasapi.remotesite.worldservice.WsProgrammeUpdate.worldServiceBuilder;

import java.io.File;

import javax.annotation.PostConstruct;

import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.media.topic.TopicStore;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.worldservice.WsProgrammeUpdate.WsProgrammeUpdateBuilder;
import org.jets3t.service.security.AWSCredentials;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.time.DayOfWeek;

@Configuration
public class WorldServicesModule {
    
    @Autowired private ContentResolver resolver; 
    @Autowired private ContentLister lister;
    @Autowired private ContentWriter writer;
    @Autowired private TopicStore topicStore;
    @Autowired private AdapterLog log;
    @Autowired private SimpleScheduler scheduler;
    
    private @Value("${s3.access}") String s3access;
    private @Value("${s3.secret}") String s3secret;
    private @Value("${s3.worldservice.bucket}") String s3bucket;
    private @Value("${worldservice.filesPath}") String wsFile;
    
    @Bean public WsDataStore wsDataStore() {
        return new GzipWsDataStore(new CopyingWsDataStore(new S3WsDataStore(new AWSCredentials(s3access, s3secret), s3bucket, log), new LocalWsDataStore(new File(wsFile)), log)) ;
    }

    @Bean protected WsProgrammeUpdateBuilder worldServiceUpdateBuilder() {
        return worldServiceBuilder(wsDataStore(), new DefaultWsSeriesHandler(resolver, writer, log), new DefaultWsProgrammeHandler(resolver, writer, log), log);
    }
    
    @Bean public WsUpdateController worldServiceUpdateController() {
        return new WsUpdateController(worldServiceUpdateBuilder());
    }
    
    @PostConstruct
    public void schedule() {
        scheduler.schedule(worldServiceUpdateBuilder().updateLatest().withName("WS Programme Update"), RepetitionRules.weekly(DayOfWeek.WEDNESDAY, new LocalTime(06,00,00)));
        scheduler.schedule(worldServiceTopicsUpdate().withName("WS Topics Update"), RepetitionRules.weekly(DayOfWeek.MONDAY, new LocalTime(06,00,00)));
    }

    public WsTopicsUpdate worldServiceTopicsUpdate() {
        return new WsTopicsUpdate(new S3WsTopicsClient(new AWSCredentials(s3access, s3secret), "abcip-topics", log), topicStore, lister, writer, log);
    }
    
}
