package org.atlasapi.remotesite.bt.channels;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClient;
import org.atlasapi.remotesite.bt.channels.mpxclient.GsonBtMpxClient;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;


public class BtChannelsModule {

    private static final RepetitionRule DAILY_AT_MIDNIGHT = RepetitionRules.daily(LocalTime.MIDNIGHT);
    
    @Value("${bt.channels.baseUri}")
    private String baseUri;
    
    @Autowired
    private ChannelGroupResolver channelGroupResolver;
    
    @Autowired
    private ChannelGroupWriter channelGroupWriter;
    
    @Autowired
    private ChannelResolver channelResolver;
    
    @Autowired
    private ChannelWriter channelWriter;
    
    @Autowired
    private SimpleScheduler scheduler;
    
    @Bean
    public BtMpxClient btMpxClient() {
        return new GsonBtMpxClient(httpClient(), baseUri);
    }
    
    private SimpleHttpClient httpClient() {
        return new SimpleHttpClientBuilder().build();
    }
    
    @Bean 
    public BtChannelGroupUpdater productionChannelGroupUpdater() {
        return new BtChannelGroupUpdater(btMpxClient(), Publisher.BT_TV_CHANNELS, "http://tv-channels.bt.com/", 
                "bt", channelGroupResolver, channelGroupWriter, channelResolver, channelWriter);
    }
    
    @PostConstruct
    public void scheduleTasks() {
        scheduler.schedule(productionChannelGroupUpdater().withName("BT Channel Group Ingester"), 
                DAILY_AT_MIDNIGHT);
    }
    
}
