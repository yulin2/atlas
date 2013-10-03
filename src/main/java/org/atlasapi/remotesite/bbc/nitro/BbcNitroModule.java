package org.atlasapi.remotesite.bbc.nitro;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.atlasapi.remotesite.bbc.nitro.v1.HttpNitroClient;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroClient;
import org.atlasapi.remotesite.channel4.epg.ScheduleResolverBroadcastTrimmer;
import org.atlasapi.util.GroupLock;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.util.Strings;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.net.HostSpecifier;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.atlas.glycerin.Glycerin;
import com.metabroadcast.atlas.glycerin.XmlGlycerin;
import com.metabroadcast.atlas.glycerin.XmlGlycerin.Builder;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.time.SystemClock;

@Configuration
public class BbcNitroModule {

    private @Value("${updaters.bbcnitro.enabled}") Boolean tasksEnabled;
    private @Value("${bbc.nitro.host}") String nitroHost;
    private @Value("${bbc.nitro.apiKey}") String nitroApiKey;
    private @Value("${bbc.nitro.requestsPerSecond}") Integer nitroRateLimit;
    
    private @Autowired SimpleScheduler scheduler;
    private @Autowired ContentWriter contentWriter;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired ScheduleWriter scheduleWriter;
    private @Autowired ChannelResolver channelResolver;
    
    @PostConstruct
    public void configure() {
        if (tasksEnabled) {
            scheduler.schedule(nitroScheduleUpdateTask(7, 7).withName("Nitro 15 day updater"),
                    RepetitionRules.every(Duration.standardHours(2)));
            scheduler.schedule(nitroScheduleUpdateTask(0, 0).withName("Nitro today updater"),
                    RepetitionRules.every(Duration.standardHours(2)));
        }
    }

    private ScheduledTask nitroScheduleUpdateTask(int back, int forward) {
        DayRangeChannelDaySupplier drcds = new DayRangeChannelDaySupplier(bbcChannelSupplier(), dayRangeSupplier(back, forward));
        ExecutorService executor = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("nitro %s").build());
        return new ChannelDayProcessingTask(executor, drcds, nitroChannelDayProcessor());
    }
    
    @Bean
    ScheduleDayUpdateController nitroScheduleUpdateController() {
        return new ScheduleDayUpdateController(channelResolver, nitroChannelDayProcessor());
    }

    @Bean
    ChannelDayProcessor nitroChannelDayProcessor() {
        ScheduleResolverBroadcastTrimmer scheduleTrimmer = new ScheduleResolverBroadcastTrimmer(Publisher.BBC_NITRO, scheduleResolver, contentResolver, contentWriter);
        return new NitroScheduleDayUpdater(scheduleWriter, scheduleTrimmer, nitroBroadcastHandler(), glycerin());
    }

    @Bean
    Glycerin glycerin() {
        if (!tasksEnabled && Strings.isNullOrEmpty(nitroHost) 
                || Strings.isNullOrEmpty(nitroHost)) {
            return UnconfiguredGlycerin.get();
        }
        Builder glycerin = XmlGlycerin.builder(nitroApiKey)
                .withHost(HostSpecifier.fromValid(nitroHost));
        if (nitroRateLimit != null) {
            glycerin.withLimiter(RateLimiter.create(nitroRateLimit));
        }
        return glycerin.build();
    }

    @Bean
    NitroClient nitroClient() {
        if (!tasksEnabled && Strings.isNullOrEmpty(nitroHost) 
                || Strings.isNullOrEmpty(nitroHost)) {
            return UnconfiguredNitroClient.get();
        }
        return new HttpNitroClient(HostSpecifier.fromValid(nitroHost), nitroApiKey);
    }

    @Bean
    NitroBroadcastHandler<ImmutableList<Optional<ItemRefAndBroadcast>>> nitroBroadcastHandler() {
        SystemClock clock = new SystemClock();
        return new ContentUpdatingNitroBroadcastHandler(contentResolver, contentWriter, 
                new GlycerinNitroContentAdapter(glycerin(), nitroClient(), clock), clock, GroupLock.<String>natural());
    }

    private Supplier<Range<LocalDate>> dayRangeSupplier(int back, int forward) {
        return AroundTodayDayRangeSupplier.builder()
                .withDaysBack(back)
                .withDaysForward(forward)
                .build();
    }

    private Supplier<ImmutableSet<Channel>> bbcChannelSupplier() {
        return new Supplier<ImmutableSet<Channel>>() {
            //TODO: really need that alias for bbc services...
            @Override
            public ImmutableSet<Channel> get() {
                return ImmutableSet.copyOf(Iterables.transform(BbcIonServices.services.values(),
                    new Function<String, Channel>() {
                        @Override
                        public Channel apply(String input) {
                            return channelResolver.fromUri(input).requireValue();
                        }
                    }
                ));
            }
        };
    }
    
    
}
