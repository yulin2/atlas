package org.atlasapi.remotesite.pa.channels;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;


public class ChannelNumberingFiltererTest {

    private ChannelGroupResolver channelGroupResolver = Mockito.mock(ChannelGroupResolver.class);
    private final ChannelNumberingFilterer filterer = new ChannelNumberingFilterer(channelGroupResolver);

    private Map<Long, ChannelGroup> channelGroupMapping = ImmutableMap.of(
            idFromPublisher(Publisher.PA), createChannelGroup(Publisher.PA),
            idFromPublisher(Publisher.BBC), createChannelGroup(Publisher.BBC)
            );
    
    @Before
    public void setUp() {
        for (Entry<Long, ChannelGroup> entry : channelGroupMapping.entrySet()) {
            Mockito.when(channelGroupResolver.channelGroupFor(entry.getKey())).thenReturn(Optional.of(entry.getValue()));
        }
    }
    
    @Test
    public void testFilterersNonPaNumberings() {
        ChannelNumbering paNumbering = createNumbering(Publisher.PA);
        ChannelNumbering bbcNumbering = createNumbering(Publisher.BBC);
        List<ChannelNumbering> numberings = ImmutableList.of(paNumbering, bbcNumbering);
        
        Iterable<ChannelNumbering> nonPaNumberings = filterer.filterNotEqualToGroupPublisher(numberings, Publisher.PA);
        
        assertEquals(bbcNumbering, Iterables.getOnlyElement(nonPaNumberings));
    }
    
    private long idFromPublisher(Publisher publisher) {
        return (long) publisher.ordinal();
    }

    private ChannelNumbering createNumbering(Publisher publisher) {
        return ChannelNumbering.builder()
                .withChannel(createChannel())
                .withChannelGroup(idFromPublisher(publisher))
                .build();
    }

    private Channel createChannel() {
        return Channel.builder()
                .build();
    }
    
    private ChannelGroup createChannelGroup(Publisher publisher) {
        Region region = new Region();
        region.setId(idFromPublisher(publisher));
        region.setPublisher(publisher);
        return region;
    }

}
