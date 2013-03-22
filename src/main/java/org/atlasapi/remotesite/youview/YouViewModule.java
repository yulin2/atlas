package org.atlasapi.remotesite.youview;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.atlasapi.remotesite.channel4.epg.ScheduleResolverBroadcastTrimmer;
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
    private @Autowired ScheduleWriter scheduleWriter;
    private @Autowired ScheduleResolver scheduleResolver;
    
    @PostConstruct
    public void startBackgroundTasks() {
        scheduler.schedule(youViewTodayUpdater().withName("YouView Today Updater"), EVERY_15_MINUTES);
        scheduler.schedule(youViewFortnightUpdater().withName("YouView Updater ±7 Days"), EVERY_HOUR);
    }

    @Bean
    public YouViewFortnightUpdater youViewFortnightUpdater() {
        return new YouViewFortnightUpdater(channelResolver(), youViewFetcher(), youViewChannelProcessor());
    }
        
    @Bean
    public YouViewTodayUpdater youViewTodayUpdater() {
        return new YouViewTodayUpdater(channelResolver(), youViewFetcher(), youViewChannelProcessor());
    }
    
    @Bean
    public YouViewChannelProcessor youViewChannelProcessor() {
        return new DefaultYouViewChannelProcessor(scheduleWriter, youViewElementProcessor(), broadcastTrimmer());
    }

    @Bean
    public YouViewElementProcessor youViewElementProcessor() {
        YouViewContentExtractor extractor = new YouViewContentExtractor(channelResolver());
        return new DefaultYouViewElementProcessor(extractor, contentResolver, contentWriter);
    }

    @Bean
    public YouViewScheduleFetcher youViewFetcher() {
        String url = Configurer.get("youview.url").get();
        int timeout = Configurer.get("youview.timeout").toInt();
        return new YouViewScheduleFetcher(url, timeout);
    }

    @Bean
    public YouViewChannelResolver channelResolver() {
        return new DefaultYouViewChannelResolver(channelResolver);
    }
    
    private BroadcastTrimmer broadcastTrimmer() {
        return new ScheduleResolverBroadcastTrimmer(Publisher.YOUVIEW, scheduleResolver, contentResolver, contentWriter);
    }
}
