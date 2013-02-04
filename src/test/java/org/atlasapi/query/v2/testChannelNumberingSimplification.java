package org.atlasapi.query.v2;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelStore;
import org.atlasapi.media.channel.MongoChannelGroupStore;
import org.atlasapi.media.channel.MongoChannelStore;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.v2.ChannelSimplifier;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.internal.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;


public class testChannelNumberingSimplification {
    
    private static final DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
    private static final MongoChannelGroupStore channelGroupStore = new MongoChannelGroupStore(mongo);
    private static final MongoChannelStore channelStore = new MongoChannelStore(mongo, channelGroupStore, channelGroupStore);
    
    private static final ChannelSimplifier simplifier = new ChannelSimplifier(Mockito.mock(NumberToShortStringCodec.class), channelStore, channelGroupStore);
    
    private static final long channelId = 1234L;
    private static final long channelGroupId = 5678L;
    
    @BeforeClass
    public static void setup() {
        Channel channel = Channel.builder()
                .withMediaType(MediaType.VIDEO)
                .withSource(Publisher.BBC)
            .build();
        channel.setId(channelId);
        
        channelStore.write(channel);
        
        Platform platform = new Platform();
        platform.setId(channelGroupId);
        
        channelGroupStore.store(platform);
    }
    
    @Test
    public void testChannelNumberingAfterEndDate() {
        
        Channel channel = channelStore.fromId(channelId).requireValue();
        ChannelGroup channelGroup = channelGroupStore.channelGroupFor(channelGroupId).get();
        
        ChannelNumbering numbering = ChannelNumbering.builder()
            .withChannel(channelId)
            .withChannelGroup(channelGroupId)
            .withChannelNumber("2")
            .withStartDate(new LocalDate(2000, 1, 1))
            .withEndDate(new LocalDate(2000, 2, 1))
            .build();
        
        channel.addChannelNumber(numbering);
        channelGroup.addChannelNumbering(numbering);
        
        channel = channelStore.write(channel);
        channelGroup = channelGroupStore.store(channelGroup);
        
        org.atlasapi.media.entity.simple.Channel simpleChannel = simplifier.simplify(channel, true, true, false, false);
        assertThat(simpleChannel.getChannelGroups().size(), is(1));
        
        simpleChannel = simplifier.simplify(channel, true, false, false, false);
        assertTrue(simpleChannel.getChannelGroups().isEmpty());
    }

}
