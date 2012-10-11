package org.atlasapi.remotesite.pa;

import static com.metabroadcast.common.time.DateTimeZones.UTC;
import static org.atlasapi.media.entity.MediaType.VIDEO;
import static org.atlasapi.media.entity.Publisher.METABROADCAST;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.Timestamp;
import org.atlasapi.persistence.media.channel.ChannelResolver;

@RunWith(MockitoJUnitRunner.class)
public class PaProgrammeProcessorTest {

    private final ContentWriter contentWriter = mock(ContentWriter.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ItemsPeopleWriter itemsPeopleWriter = mock(ItemsPeopleWriter.class);
    private final AdapterLog log = new NullAdapterLog();
    
    private PaProgrammeProcessor progProcessor;
    
    @Before
    public void setup() {
        when(channelResolver.fromUri(argThat(any(String.class)))).then(new Answer<Maybe<Channel>>() {
            @Override
            public Maybe<Channel> answer(InvocationOnMock invocation) throws Throwable {
                String input = (String)invocation.getArguments()[0];
                return Maybe.just(new Channel(METABROADCAST, input, input, false, VIDEO, input));
            }
        });
        progProcessor = new PaProgrammeProcessor(contentWriter, contentResolver, channelResolver, itemsPeopleWriter, log);
    }
    
    @Test
    public void testExtractsNewFilmWithEpisodeUri() {
        Channel channel = new Channel(METABROADCAST, "c", "c", false, VIDEO, "c");
        ProgData progData = new ProgData();
        progData.setProgId("1");
        progData.setRtFilmnumber("5");
        progData.setDuration("1");
        progData.setDate("06/08/2012");
        progData.setTime("11:40");
        when(contentResolver.findByCanonicalUris(ImmutableList.of(
            "http://pressassociation.com/films/5",
            "http://pressassociation.com/episodes/1"
        ))).thenReturn(ResolvedContent.builder().build());
        
        progProcessor.process(progData, channel, UTC, Timestamp.of(0));

        ArgumentCaptor<Item> argCaptor = ArgumentCaptor.forClass(Item.class);
        verify(contentWriter).createOrUpdate(argCaptor.capture());
        
        Item written = argCaptor.getValue();
        
        assertThat(written.getCanonicalUri(), is("http://pressassociation.com/episodes/1"));
        assertThat(written.getCurie(), is("pa:e-1"));
        assertThat(written.getAliases(), hasItem("http://pressassociation.com/films/5"));
        
    }
    
    @Test
    public void testAddsEpisodesAliasForFilmWithRtFilmNumberUri() {
        Channel channel = new Channel(METABROADCAST, "c", "c", false, VIDEO, "c");
        ProgData progData = new ProgData();
        progData.setProgId("1");
        progData.setRtFilmnumber("5");
        progData.setDuration("1");
        progData.setDate("06/08/2012");
        progData.setTime("11:40");
        
        Film film = new Film("http://pressassociation.com/films/5", "pa:f-5", Publisher.PA);
        Version version = new Version();
        version.setProvider(Publisher.PA);
        film.addVersion(version);
        
        when(contentResolver.findByCanonicalUris(ImmutableList.of(
            "http://pressassociation.com/films/5",
            "http://pressassociation.com/episodes/1"
        ))).thenReturn(ResolvedContent.builder()
            .put("http://pressassociation.com/films/5", film)
            .build() 
        );
        
        progProcessor.process(progData, channel, UTC, Timestamp.of(0));

        ArgumentCaptor<Item> argCaptor = ArgumentCaptor.forClass(Item.class);
        verify(contentWriter).createOrUpdate(argCaptor.capture());
        
        Item written = argCaptor.getValue();
        
        assertThat(written.getAliases(), hasItem("http://pressassociation.com/episodes/1"));
        
    }

    @Test
    public void testAddsRtFilmNumberAliasForFilmWithEpisodesUri() {
        Channel channel = new Channel(METABROADCAST, "c", "c", false, VIDEO, "c");
        ProgData progData = new ProgData();
        progData.setProgId("1");
        progData.setRtFilmnumber("5");
        progData.setDuration("1");
        progData.setDate("06/08/2012");
        progData.setTime("11:40");
        
        Film film = new Film("http://pressassociation.com/episodes/1", "pa:e-1", Publisher.PA);
        Version version = new Version();
        version.setProvider(Publisher.PA);
        film.addVersion(version);
        
        when(contentResolver.findByCanonicalUris(ImmutableList.of(
            "http://pressassociation.com/films/5",
            "http://pressassociation.com/episodes/1"
        ))).thenReturn(ResolvedContent.builder()
            .put("http://pressassociation.com/episodes/1", film)
            .build() 
        );
        
        progProcessor.process(progData, channel, UTC, Timestamp.of(0));

        ArgumentCaptor<Item> argCaptor = ArgumentCaptor.forClass(Item.class);
        verify(contentWriter).createOrUpdate(argCaptor.capture());
        
        Item written = argCaptor.getValue();
        
        assertThat(written.getAliases(), hasItem("http://pressassociation.com/films/5"));
        
    }

}
