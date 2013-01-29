package org.atlasapi.remotesite.pa;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.util.WriteResult;
import org.atlasapi.persistence.content.people.DummyItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
import org.atlasapi.remotesite.pa.persistence.PaScheduleVersionStore;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.MoreExecutors;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.collect.ImmutableOptionalMap;

@RunWith(MockitoJUnitRunner.class)
public class PaBaseProgrammeUpdaterTest {

    private final ContentStore contentStore = mock(ContentStore.class);
    private final ChannelResolver channelResolver = new DummyChannelResolver();
    private final AdapterLog log = new NullAdapterLog(); 
    private final PaProgDataProcessor programmeProcessor = new PaProgrammeProcessor(contentStore, channelResolver, new DummyItemsPeopleWriter(), log);

    private static long id = 0;
    
    @Test
    public <T> void testShouldCreateCorrectPaData() throws Exception {
        PaScheduleVersionStore scheduleVersionStore = mock(PaScheduleVersionStore.class);
        Channel channel = channelResolver.fromUri("http://www.bbc.co.uk/bbcone").requireValue();
        LocalDate scheduleDay = new LocalDate(2011, DateTimeConstants.JANUARY, 15);

        when(scheduleVersionStore.get(channel, scheduleDay))
            .thenReturn(Optional.<Long>absent())
            .thenReturn(Optional.<Long>of(1L))
            .thenReturn(Optional.<Long>of(201202251115L))
            .thenReturn(Optional.<Long>of(201202251115L));
        when(contentStore.writeContent(argThat(any(Content.class))))
            .then(new Answer<WriteResult<Content>>() {
                @Override
                public WriteResult<Content> answer(InvocationOnMock invocation) throws Throwable {
                    Content written = (Content) invocation.getArguments()[0];
                    written.setId(id++);
                    return WriteResult.written(written).build();
                }
            });
        when(contentStore.resolveAliases(anyCollectionOf(String.class), argThat(is(Publisher.PA))))
            .thenReturn(ImmutableOptionalMap.fromMap(ImmutableMap.<String, Content>of()));
        
        TestPaProgrammeUpdater updater = new TestPaProgrammeUpdater(
            programmeProcessor, channelResolver, scheduleVersionStore, log,
                new File(Resources.getResource("20110115_tvdata.xml").getFile()), 
                new File(Resources.getResource("201202251115_20110115_tvdata.xml").getFile())
        );
        updater.run();
        
        verify(scheduleVersionStore).store(channel, scheduleDay, 1);
        verify(scheduleVersionStore).store(channel, scheduleDay, 201202251115L);
        
        ArgumentCaptor<Content> contentCapture = ArgumentCaptor.forClass(Content.class);
        verify(contentStore, atLeast(2)).writeContent(contentCapture.capture());
        
        List<Content> capturedContent = contentCapture.getAllValues();
        String brandUri = "http://pressassociation.com/brands/122139";
        String itemUri = "http://pressassociation.com/episodes/1424497";
        Identified content = findContent(capturedContent, brandUri);

        assertNotNull(content);
        assertTrue(content instanceof Brand);
        Brand brand = (Brand) content;
        assertNotNull(brand.getImage());

        Item item = (Item) findContent(capturedContent, itemUri);
        assertTrue(item.getCanonicalUri().contains("episodes"));
        assertNotNull(item.getImage());
        assertFalse(item.getVersions().isEmpty());
        assertEquals(MediaType.VIDEO, item.getMediaType());
        assertEquals(Specialization.TV, item.getSpecialization());

        assertEquals(17, item.people().size());
        assertEquals(14, item.actors().size());

        Version version = item.getVersions().iterator().next();
        assertFalse(version.getBroadcasts().isEmpty());

        Broadcast broadcast = version.getBroadcasts().iterator().next();
        assertEquals("pa:71118472", broadcast.getSourceId());

        updater.run();
        Thread.sleep(1000);

        contentCapture = ArgumentCaptor.forClass(Content.class);
        verify(contentStore, atLeast(2)).writeContent(contentCapture.capture());
        
        content = findContent(contentCapture.getAllValues(), brandUri);

        assertNotNull(content);
        assertTrue(content instanceof Brand);
        brand = (Brand) content;

        Item item2 = (Item) findContent(capturedContent, itemUri);
        assertFalse(item2.getVersions().isEmpty());

        version = item2.getVersions().iterator().next();
        assertFalse(version.getBroadcasts().isEmpty());

        broadcast = version.getBroadcasts().iterator().next();
        assertEquals("pa:71118472", broadcast.getSourceId());

//        // Test people get created
//        for (CrewMember crewMember : item.people()) {
//            content = store.findByCanonicalUri(crewMember.getCanonicalUri());
//            assertTrue(content instanceof Person);
//            assertEquals(crewMember.name(), ((Person) content).name());
//        }
    }



    private Identified findContent(List<Content> capturedContent, String uri) {
        Iterator<Content> iterator = Lists.reverse(capturedContent).iterator();
        while(iterator.hasNext()) {
            Content next = iterator.next();
            if (uri.equals(next.getCanonicalUri())) {
                return next;
            }
        }
        return null;
    }

    static class TestPaProgrammeUpdater extends PaBaseProgrammeUpdater {

        private List<File> files;

        public TestPaProgrammeUpdater(PaProgDataProcessor processor, ChannelResolver channelResolver, PaScheduleVersionStore scheduleVersionStore, AdapterLog log, File... files) {
            super(MoreExecutors.sameThreadExecutor(), new PaChannelProcessor(processor, scheduleVersionStore), new DefaultPaProgrammeDataStore("/data/pa", null), channelResolver, Optional.fromNullable(scheduleVersionStore));
            this.files = ImmutableList.copyOf(files);
        }

        @Override
        public void runTask() {
            this.processFiles(files);
        }
    }
    
    static class DummyChannelResolver implements ChannelResolver {

		@Override
		public Maybe<Channel> fromKey(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Maybe<Channel> fromId(Id id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Maybe<Channel> fromUri(String uri) {
			if("http://www.bbc.co.uk/bbcone".equals(uri)) {
				return Maybe.just(new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", MediaType.VIDEO, "http://www.bbc.co.uk/bbcone"));
			}
			return Maybe.just(new Channel());
		}

		@Override
		public Iterable<Channel> all() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, Channel> forAliases(String aliasPrefix) {
			return ImmutableMap.of(
					"http://pressassociation.com/channels/4", new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", MediaType.VIDEO, "http://www.bbc.co.uk/bbcone"));
		}

        @Override
        public Iterable<Channel> forIds(Iterable<Id> ids) {
            throw new UnsupportedOperationException();
        }
    	
    }
   
}
