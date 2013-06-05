package org.atlasapi.remotesite.pa;

import static com.metabroadcast.common.time.DateTimeZones.UTC;
import static org.atlasapi.media.entity.MediaType.VIDEO;
import static org.atlasapi.media.entity.Publisher.METABROADCAST;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Image;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.remotesite.pa.listings.bindings.PictureUsage;
import org.atlasapi.remotesite.pa.listings.bindings.Pictures;
import org.atlasapi.remotesite.pa.listings.bindings.ProgData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.time.Timestamp;

@RunWith(MockitoJUnitRunner.class)
public class PaProgrammeProcessorTest {

    private final ContentStore contentStore = mock(ContentStore.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ItemsPeopleWriter itemsPeopleWriter = mock(ItemsPeopleWriter.class);
    private final Described described = mock(Described.class);
    private final AdapterLog log = new NullAdapterLog();
    
    @Captor
    private ArgumentCaptor<Iterable<Image>> imageListCaptor;
    
    private PaProgrammeProcessor progProcessor;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
        when(channelResolver.fromUri(argThat(any(String.class)))).then(new Answer<Maybe<Channel>>() {
            @Override
            public Maybe<Channel> answer(InvocationOnMock invocation) throws Throwable {
                String input = (String)invocation.getArguments()[0];
                return Maybe.just(new Channel(METABROADCAST, input, input, false, VIDEO, input));
            }
        });
        progProcessor = new PaProgrammeProcessor(contentStore, channelResolver, itemsPeopleWriter);
    }
    
    @Test 
    public void testSetsPrimaryImages() {
        Pictures pictures = new Pictures();
        
        initialisePictures(pictures);
        
        progProcessor.selectImages(pictures, described, PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND, Maybe.<String>nothing());
        verify(described).setImages(imageListCaptor.capture());
        verify(described).setImage(PaProgrammeProcessor.PA_BASE_IMAGE_URL+"series1");
        verifyNoMoreInteractions(described);
        
        Iterator<Image> iter = imageListCaptor.getValue().iterator();
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series1", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series2", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series3", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series4", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series5", iter.next().getCanonicalUri());
        assertFalse(iter.hasNext());
    }
    
    @Test 
    public void testSetsPrimaryImagesSkippingFallback() {
        Pictures pictures = new Pictures();
        
        initialisePictures(pictures);
        
        progProcessor.selectImages(pictures, described, PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND, PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, Maybe.<String>nothing());
        verify(described).setImages(imageListCaptor.capture());
        
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(described, atLeastOnce()).setImage(stringCaptor.capture());
        verifyNoMoreInteractions(described);
        
        assertEquals(PaProgrammeProcessor.PA_BASE_IMAGE_URL+"brand1", stringCaptor.getValue()); // The latest version should be...
        
        Iterator<Image> iter = imageListCaptor.getValue().iterator();
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"brand1", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"brand2", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"brand3", iter.next().getCanonicalUri());
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"brand4", iter.next().getCanonicalUri());
        assertFalse(iter.hasNext());
    }
    
    @Test
    public void testUsesFallbackImage() {
        Pictures pictures = new Pictures();
        
        initialisePictures(pictures);
        
        progProcessor.selectImages(pictures, described, PaProgrammeProcessor.PA_PICTURE_TYPE_EPISODE, PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, Maybe.<String>nothing());
        verify(described).setImages(imageListCaptor.capture());
        verify(described).setImage(PaProgrammeProcessor.PA_BASE_IMAGE_URL+"series1");
        verifyNoMoreInteractions(described);
        
        Iterator<Image> iter = imageListCaptor.getValue().iterator();
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series1", iter.next().getCanonicalUri());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testUsesPreferredFallbackImage() {
        Pictures pictures = new Pictures();
        
        initialisePictures(pictures);
        
        progProcessor.selectImages(pictures, described, PaProgrammeProcessor.PA_PICTURE_TYPE_EPISODE, PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, Maybe.just(PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND));
        verify(described).setImages(imageListCaptor.capture());
        verify(described).setImage(PaProgrammeProcessor.PA_BASE_IMAGE_URL+"series1");
        verifyNoMoreInteractions(described);
        
        Iterator<Image> iter = imageListCaptor.getValue().iterator();
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"series1", iter.next().getCanonicalUri());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testUsesPreferredFallbackImageInOrder() {
        Pictures pictures = new Pictures();
        
        initialisePictures(pictures);
        
        progProcessor.selectImages(pictures, described, PaProgrammeProcessor.PA_PICTURE_TYPE_EPISODE, PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND, Maybe.just(PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES));
        verify(described).setImages(imageListCaptor.capture());
        
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(described, atLeastOnce()).setImage(stringCaptor.capture());
        verifyNoMoreInteractions(described);
        
        assertEquals(PaProgrammeProcessor.PA_BASE_IMAGE_URL+"brand1", stringCaptor.getValue()); // The latest version should be...
        
        Iterator<Image> iter = imageListCaptor.getValue().iterator();
        assertEquals(PaProgrammeProcessor.NEW_IMAGE_BASE_IMAGE_URL+"brand1", iter.next().getCanonicalUri());
        assertFalse(iter.hasNext());
    }

    private void initialisePictures(Pictures pictures) {
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, "series1"));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND,  "brand1" ));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, "series2"));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, "series3"));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND,  "brand2" ));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND,  "brand3" ));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, "series4"));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_SERIES, "series5"));
        pictures.getPictureUsage().add(createPictureUsage(PaProgrammeProcessor.PA_PICTURE_TYPE_BRAND,  "brand4" ));
    }
    
    private PictureUsage createPictureUsage(String type, String uri) {
        PictureUsage usage = new PictureUsage();
        usage.setType(type);
        usage.setvalue(uri);
        return usage;
    }
    
    @Test
    @Ignore
    public void testExtractsNewFilmWithEpisodeUri() {
        Channel channel = new Channel(METABROADCAST, "c", "c", false, VIDEO, "c");
        ProgData progData = new ProgData();
        progData.setProgId("1");
        progData.setRtFilmnumber("5");
        progData.setDuration("1");
        progData.setDate("06/08/2012");
        progData.setTime("11:40");
        when(contentStore.resolveAliases(ImmutableList.of(
            PaHelper.getFilmAlias("5"),
            PaHelper.getEpisodeAlias("1")
        ), Publisher.PA))
            .thenReturn(ImmutableOptionalMap.<Alias,Content>of());
        
        progProcessor.process(progData, channel, UTC, Timestamp.of(0));

        ArgumentCaptor<Item> argCaptor = ArgumentCaptor.forClass(Item.class);
        verify(contentStore).writeContent(argCaptor.capture());
        
        Item written = argCaptor.getValue();
        
        assertThat(written.getCanonicalUri(), is("http://pressassociation.com/episodes/1"));
        assertThat(written.getCurie(), is("pa:e-1"));
        // TODO new aliases
        assertThat(written.getAliasUrls(), hasItem("http://pressassociation.com/films/5"));
        
    }
    
    @Test
    @Ignore
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
        
        when(contentStore.resolveAliases(ImmutableList.of(
            PaHelper.getFilmAlias("5"),
            PaHelper.getEpisodeAlias("1")
        ), Publisher.PA))
            .thenReturn(ImmutableOptionalMap.<Alias,Content>of(PaHelper.getFilmAlias("5"), film));
        
        progProcessor.process(progData, channel, UTC, Timestamp.of(0));

        ArgumentCaptor<Item> argCaptor = ArgumentCaptor.forClass(Item.class);
        verify(contentStore).writeContent(argCaptor.capture());
        
        Item written = argCaptor.getValue();
        
        // TODO new aliases
        assertThat(written.getAliasUrls(), hasItem("http://pressassociation.com/episodes/1"));
        
    }

    @Test
    @Ignore
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
        
        when(contentStore.resolveAliases(ImmutableList.of(
                PaHelper.getFilmAlias("5"),
                PaHelper.getEpisodeAlias("1")
            ), Publisher.PA))
            .thenReturn(ImmutableOptionalMap.<Alias,Content>of(PaHelper.getEpisodeAlias("1"), film));
        
        progProcessor.process(progData, channel, UTC, Timestamp.of(0));

        ArgumentCaptor<Item> argCaptor = ArgumentCaptor.forClass(Item.class);
        verify(contentStore).writeContent(argCaptor.capture());
        
        Item written = argCaptor.getValue();
        
        // TODO new aliases
        assertThat(written.getAliasUrls(), hasItem("http://pressassociation.com/films/5"));
        
    }

}
