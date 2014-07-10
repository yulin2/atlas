package org.atlasapi.remotesite.btvod;

import java.io.File;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BtVodModule {

    private static final String URI_PREFIX = "http://vod.bt.com/";
    
    @Autowired
    private SimpleScheduler scheduler;
    @Autowired
    private ContentResolver contentResolver;
    @Autowired
    private ContentWriter contentWriter;
    @Value("${bt.vod.file}")
    private String filename;
    
    @Bean
    public BtVodBrandHierarchyExtractor btVodUpdater() {
        return new BtVodBrandHierarchyExtractor(contentResolver, 
                contentWriter, btVodData(), URI_PREFIX, Publisher.BT_VOD);
    }
    
    private BtVodData btVodData() {
        return new BtVodData(Files.asCharSource(new File(filename), Charsets.UTF_8));
    }
    
    @PostConstruct
    public void scheduleTask() {
        scheduler.schedule(btVodUpdater(), RepetitionRules.NEVER);
    }
}
