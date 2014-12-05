package org.atlasapi.remotesite.rovi.processing;

import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.rovi.populators.ScheduleLineBroadcastExtractor;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;


public class ScheduleFileProcessorTest {

    private static final String CHANNEL_URI_PREFIX = "http://rovicorp.com/channels/";

    private static final String PROGRAM1_URI = canonicalUriForProgram("1");
    private static final String PROGRAM2_URI = canonicalUriForProgram("2");

    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final ContentWriter contentWriter = mock(ContentWriter.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    
    private final ScheduleLineBroadcastExtractor scheduleLineBroadcastExtractor = new ScheduleLineBroadcastExtractor(channelResolver);
    private final ItemBroadcastUpdater itemBroadcastUpdater = new ItemBroadcastUpdater(contentResolver, contentWriter);

    private final Channel channel = Channel.builder()
            .withUri("http://rovicorp.com/channels/123")
            .build();

    @Before
    public void init() {
        Mockito.reset(contentResolver);
        Mockito.reset(contentWriter);

        when(channelResolver.forAlias(CHANNEL_URI_PREFIX + "30863")).thenReturn(Maybe.just(channel));
    }
    
    @Test
    public void testProcessFile() throws IOException {
        ScheduleFileProcessor scheduleFileProcessor = new ScheduleFileProcessor(itemBroadcastUpdater,
                scheduleLineBroadcastExtractor,
                true);

        Item testItem1 = testItem(PROGRAM1_URI);
        Item testItem2 = testItem(PROGRAM2_URI);

        when(contentResolver.findByCanonicalUris(ImmutableSet.of(PROGRAM1_URI))).thenReturn(resolvedContentFor(testItem1));
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(PROGRAM2_URI))).thenReturn(resolvedContentFor(testItem2));
        
        scheduleFileProcessor.process(new File(Resources.getResource("org/atlasapi/remotesite/rovi/schedule.txt").getFile()));
        
        verify(contentWriter).createOrUpdate(testItem1);
        verify(contentWriter).createOrUpdate(testItem2);
    }

    @Test
    public void testFullIngestShouldReplaceExistentBroadcasts() throws IOException {
        ScheduleFileProcessor scheduleFileProcessor = new ScheduleFileProcessor(itemBroadcastUpdater,
                scheduleLineBroadcastExtractor,
                true);

        Item testItem1 = testItemWithBroadcasts(PROGRAM1_URI, existingBroadcasts());
        Item testItem2 = testItem(PROGRAM2_URI);

        when(contentResolver.findByCanonicalUris(ImmutableSet.of(PROGRAM1_URI))).thenReturn(resolvedContentFor(testItem1));
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(PROGRAM2_URI))).thenReturn(resolvedContentFor(testItem2));

        scheduleFileProcessor.process(new File(Resources.getResource("org/atlasapi/remotesite/rovi/schedule.txt").getFile()));

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        verify(contentWriter, times(2)).createOrUpdate(argument.capture());

        Map<String, Item> items = getItemsMap(argument);
        Item writtenItem1 = items.get(PROGRAM1_URI);
        Set<Broadcast> newBroadcasts = Iterables.getOnlyElement(writtenItem1.getVersions()).getBroadcasts();

        assertTrue(Sets.intersection(newBroadcasts, existingBroadcasts()).isEmpty());
    }

    @Test
    public void testFullIngestShouldMergeExistentBroadcasts() throws IOException {
        reset(contentResolver);
        ScheduleFileProcessor scheduleFileProcessor = new ScheduleFileProcessor(itemBroadcastUpdater,
                scheduleLineBroadcastExtractor,
                false);

        Item testItem1 = testItemWithBroadcasts(PROGRAM1_URI, existingBroadcasts());
        Item testItem2 = testItem(PROGRAM2_URI);

        when(contentResolver.findByCanonicalUris(ImmutableSet.of(PROGRAM1_URI))).thenReturn(resolvedContentFor(testItem1));
        when(contentResolver.findByCanonicalUris(ImmutableSet.of(canonicalUriForProgram("2")))).thenReturn(
                resolvedContentFor(testItem2));

        scheduleFileProcessor.process(new File(Resources.getResource(
                "org/atlasapi/remotesite/rovi/schedule.txt").getFile()));

        ArgumentCaptor<Item> argument = ArgumentCaptor.forClass(Item.class);

        verify(contentWriter, times(2)).createOrUpdate(argument.capture());

        Map<String, Item> items = getItemsMap(argument);
        Item writtenItem1 = items.get(PROGRAM1_URI);
        Set<Broadcast> newBroadcasts = Iterables.getOnlyElement(writtenItem1.getVersions()).getBroadcasts();

        assertEquals(existingBroadcasts().size(), Sets.intersection(newBroadcasts, existingBroadcasts()).size());
    }

    private Set<Broadcast> existingBroadcasts() {
        return ImmutableSet.of(
                new Broadcast("channel1",
                        DateTime.parse("2014-11-01T10:00"),
                        DateTime.parse("2014-11-01T11:00")),
                new Broadcast("channel2",
                        DateTime.parse("2014-11-01T11:00"),
                        DateTime.parse("2014-11-01T12:00"))
        );
    }
    
    private Item testItem(String uri) {
        return ComplexItemTestDataBuilder.complexItem()
                                         .withUri(uri)
                                         .withVersions(new Version())
                                         .build();
    }

    private Item testItemWithBroadcasts(String uri, Set<Broadcast> broadcasts) {
        Version version = new Version();
        version.setBroadcasts(broadcasts);

        return ComplexItemTestDataBuilder.complexItem()
                .withUri(uri)
                .withVersions(version)
                .build();
    }
    
    private ResolvedContent resolvedContentFor(Item i) {
        return ResolvedContent.builder()
                              .put(i.getCanonicalUri(), i)
                              .build();
    }

    private Map<String, Item> getItemsMap(ArgumentCaptor<Item> argument) {
        return Maps.uniqueIndex(argument.getAllValues(), new Function<Item, String>() {

            @Override public String apply(Item item) {
                return item.getCanonicalUri();
            }
        });
    }
}
