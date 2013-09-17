package org.atlasapi.remotesite.bbc.nitro;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.atlasapi.remotesite.bbc.ion.BbcIonServices;
import org.atlasapi.remotesite.channel4.epg.ScheduleResolverBroadcastTrimmer;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.util.Strings;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.atlas.glycerin.Glycerin;
import com.metabroadcast.atlas.glycerin.XmlGlycerin;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BbcNitroModule {

    private @Value("${updaters.bbcnitro.enabled}") Boolean tasksEnabled;
    private @Value("${bbc.nitro.host}") String nitroHost;
    private @Value("${bbc.nitro.apiKey}") String nitroApiKey;
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired ScheduleWriter scheduleWriter;
    private @Autowired ChannelResolver channelResolver;
    
    @PostConstruct
    public void configure() {
        scheduler.schedule(nitroScheduleUpdateTask(7, 7).withName("Nitro 15 day updater"),
                RepetitionRules.every(Duration.standardHours(2)));
        scheduler.schedule(nitroScheduleUpdateTask(0, 0).withName("Nitro today updater"),
                RepetitionRules.every(Duration.standardHours(2)));
        System.out.println(BbcIonServices.services.values().size());
    }

    private ScheduledTask nitroScheduleUpdateTask(int back, int forward) {
        return new ChannelDayProcessingTask(bbcChannelSupplier(), dayRangeSupplier(back, forward), nitroChannelDayProcessor(), false);
    }
    
    @Bean
    ScheduleDayUpdateController nitroScheduleUpdateController() {
        return new ScheduleDayUpdateController(channelResolver, nitroChannelDayProcessor());
    }

    @Bean
    ChannelDayProcessor nitroChannelDayProcessor() {
        return new NitroScheduleDayUpdater(scheduleWriter, new ScheduleResolverBroadcastTrimmer(Publisher.BBC_NITRO, scheduleResolver, contentResolver, contentWriter), nitroBroadcastHandler(), glycerin());
    }

    @Bean
    Glycerin glycerin() {
        if (!tasksEnabled && Strings.isNullOrEmpty(nitroHost) 
                || Strings.isNullOrEmpty(nitroHost)) {
            return UnconfiguredGlycerin.get();
        }
        return XmlGlycerin.builder(nitroApiKey)
                .withHost(HostSpecifier.fromValid(nitroHost))
                .build();
    }

    @Bean
    NitroBroadcastHandler<ItemRefAndBroadcast> nitroBroadcastHandler() {
        return new ContentUpdatingNitroBroadcastHandler(contentResolver, contentWriter, 
                new GlycerinNitroContentAdapter(glycerin()));
    }

    private Supplier<Range<LocalDate>> dayRangeSupplier(int back, int forward) {
        return AroundTodayDayRangeSupplier.builder()
                .withDaysBack(back)
                .withDaysForward(forward)
                .build();
    }

    private Supplier<Iterable<Channel>> bbcChannelSupplier() {
        return new Supplier<Iterable<Channel>>() {
            //TODO: really need that alias for bbc services...
            @Override
            public Iterable<Channel> get() {
                return Iterables.transform(BbcIonServices.services.values(),
                        new Function<String, Channel>() {
                            @Override
                            public Channel apply(String input) {
                                return channelResolver.fromUri(input).requireValue();
                            }
                        });
            }
            
        };
    }
    
    
}
