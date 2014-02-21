package org.atlasapi.remotesite.rovi.processing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.rovi.RoviUtils;
import org.atlasapi.remotesite.rovi.populators.ScheduleLineBroadcastExtractor;
import org.atlasapi.remotesite.rovi.processing.ItemBroadcastUpdater;
import org.atlasapi.remotesite.rovi.processing.ScheduleFileProcessor;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;


public class ScheduleFileProcessorTest {

    private static final String CHANNEL_URI_PREFIX = "http://rovicorp.com/channels/";
    
    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final ContentWriter contentWriter = mock(ContentWriter.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    
    private final ScheduleLineBroadcastExtractor scheduleLineBroadcastExtractor = new ScheduleLineBroadcastExtractor(channelResolver);
    private final ItemBroadcastUpdater itemBroadcastUpdater = new ItemBroadcastUpdater(contentResolver, contentWriter);
    private final ScheduleFileProcessor scheduleFileProcessor = new ScheduleFileProcessor(itemBroadcastUpdater, scheduleLineBroadcastExtractor);
    
    private final Item testItem1 = testItem(RoviUtils.canonicalUriForProgram("1"));
    private final Item testItem2 = testItem(RoviUtils.canonicalUriForProgram("2"));
    
    private final Channel channel = Channel.builder()
            .withUri("http://rovicorp.com/channels/123")
            .build();   
    
    @Test
    public void testProcessFile() throws IOException {
        when(channelResolver.forAlias(CHANNEL_URI_PREFIX + "30863")).thenReturn(Maybe.just(channel));
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(RoviUtils.canonicalUriForProgram("1")))).thenReturn(resolvedContentFor(testItem1));
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(RoviUtils.canonicalUriForProgram("2")))).thenReturn(resolvedContentFor(testItem2));
        
        scheduleFileProcessor.process(new File(Resources.getResource("org/atlasapi/remotesite/rovi/schedule.txt").getFile()));
        
        verify(contentWriter).createOrUpdate(testItem1);
        verify(contentWriter).createOrUpdate(testItem2);
    }
    
    private Item testItem(String uri) {
        return ComplexItemTestDataBuilder.complexItem()
                                         .withUri(uri)
                                         .withVersions(new Version())
                                         .build();
    }
    
    private ResolvedContent resolvedContentFor(Item i) {
        return ResolvedContent.builder()
                              .put(i.getCanonicalUri(), i)
                              .build();
    }
}
