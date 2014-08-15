package org.atlasapi.remotesite.bt.channels;

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.annotation.PostConstruct;

import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClient;
import org.atlasapi.remotesite.bt.channels.mpxclient.GsonBtMpxClient;
import org.atlasapi.remotesite.pa.PaModule;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Import( PaModule.class )
public class BtChannelsModule {

    private static final String URI_PREFIX = "http://tv-channels.bt.com/";

    private static final RepetitionRule DAILY_AT_3PM = RepetitionRules.daily(new LocalTime(15,0));
    
    @Value("${bt.channels.baseUri}")
    private String baseUri;
    
    @Value("${bt.channels.freeviewPlatformChannelGroupId}")
    private String freeviewPlatformChannelGroupId;
    
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
    
    @Autowired
    private WriteLock channelWriterLock;
    
    @Bean
    public BtMpxClient btMpxClient() {
        return new GsonBtMpxClient(httpClient(), baseUri);
    }
    
    private SimpleHttpClient httpClient() {
        return new SimpleHttpClientBuilder().build();
    }
    
    @Bean 
    public BtChannelGroupUpdater productionChannelGroupUpdater() {
        return new BtChannelGroupUpdater(btMpxClient(), Publisher.BT_TV_CHANNELS, 
                URI_PREFIX,  "bt", channelGroupResolver, channelGroupWriter, 
                channelResolver, channelWriter, allChannelsUpdater(), channelWriterLock);
    }
    
    @Bean
    public BtAllChannelsChannelGroupUpdater allChannelsUpdater() {
        return new BtAllChannelsChannelGroupUpdater(channelGroupWriter, 
                channelGroupResolver, freeviewPlatformChannelGroupId, 
                URI_PREFIX, Publisher.BT_TV_CHANNELS);
    }
    @PostConstruct
    public void scheduleTasks() {
        scheduler.schedule(productionChannelGroupUpdater()
                .withName("BT Channel Group Ingester"), 
                DAILY_AT_3PM);
    }
    
}
