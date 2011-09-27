package org.atlasapi.remotesite.worldservice;

import static org.atlasapi.remotesite.worldservice.WsProgrammeUpdate.worldServiceBuilder;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.worldservice.WsProgrammeUpdate.WsProgrammeUpdateBuilder;
import org.jets3t.service.security.AWSCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class WorldServicesModule {
    
    @Autowired private ContentResolver resolver; 
    @Autowired private ContentWriter writer;
    @Autowired private AdapterLog log;
    @Autowired private SimpleScheduler scheduler;
    
    private @Value("${s3.access}") String s3access;
    private @Value("${s3.secret}") String s3secret;
    private @Value("${s3.worldservice.bucket}") String s3bucket;
    
    @Bean public WsDataStore wsDataStore() {
        return new S3WsDataStore(new AWSCredentials(s3access, s3secret), s3bucket, log);
    }

    @Bean protected WsProgrammeUpdateBuilder worldServiceUpdateBuilder() {
        return worldServiceBuilder(wsDataStore(), new DefaultWsSeriesHandler(resolver, writer, log), new DefaultWsProgrammeHandler(resolver, writer, log), log);
    }
    
    @Bean public WsUpdateController worldServiceUpdateController() {
        return new WsUpdateController(worldServiceUpdateBuilder());
    }
    
    @PostConstruct
    public void schedule() {
        scheduler.schedule(worldServiceUpdateBuilder().updateLatest(), RepetitionRules.NEVER);
    }
    
}
