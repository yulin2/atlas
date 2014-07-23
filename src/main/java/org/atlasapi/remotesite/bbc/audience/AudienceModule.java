package org.atlasapi.remotesite.bbc.audience;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class AudienceModule {

    @Autowired
    private SimpleScheduler scheduler;
    
    @Autowired
    private ScheduleResolver scheduleResolver;
    
    @Autowired
    private ChannelResolver channelResolver;
    
    @Autowired
    private ContentWriter contentWriter;
    
    @Value("${bbc.audience-data.filename}")
    private String filename;
    
    @Bean
    public AudienceDataProcessor audienceDataProcessor() {
        return new AudienceDataProcessor(scheduleResolver, channelResolver, contentWriter, audienceDataReader());
    }
    
    @Bean
    public AudienceDataReader audienceDataReader() {
        return new AudienceDataReader(filename);
    }
    
    @PostConstruct
    public void postConstruct() {
        scheduler.schedule(audienceDataProcessor().withName("BBC Audience Data ingest"), RepetitionRules.NEVER);
    }
}
