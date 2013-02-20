package org.atlasapi.remotesite.youview;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Every;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class YouViewModule {
    private final static Every EVERY_15_MINUTES = RepetitionRules.every(Duration.standardMinutes(15));
    private final static Every EVERY_HOUR = RepetitionRules.every(Duration.standardHours(1));
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ContentWriter contentWriter;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(youViewTodayUpdater().withName("YouView Today Updater"), EVERY_15_MINUTES);
        scheduler.schedule(youViewFortnightUpdater().withName("YouView Updater ±7 Days"), EVERY_HOUR);
    }

    private YouViewFortnightUpdater youViewFortnightUpdater() {
        return new YouViewFortnightUpdater(youViewChannelResolver(), youViewFetcher(), youViewXmlElementHandler());
    }

    private YouViewTodayUpdater youViewTodayUpdater() {
        return new YouViewTodayUpdater(youViewChannelResolver(), youViewFetcher(), youViewXmlElementHandler());
    }
    
    private YouViewScheduleFetcher youViewFetcher() {
        String url = Configurer.get("youview.url").get();
        int timeout = Configurer.get("youview.timeout").toInt();
        return new YouViewScheduleFetcher(url, timeout);
    }
    
    private YouViewXmlElementHandler youViewXmlElementHandler() {
        YouViewContentExtractor extractor = new YouViewContentExtractor(youViewChannelResolver());
        return new DefaultYouViewXmlElementHandler(extractor, contentResolver, contentWriter);
    }
    
    @Bean
    YouViewChannelResolver youViewChannelResolver() {  
        return new DefaultYouViewChannelResolver(channelResolver);
    }
}
