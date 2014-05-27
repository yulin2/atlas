package org.atlasapi.remotesite.pa.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.pa.PaHelper;
import org.atlasapi.remotesite.pa.features.PaFeaturesContentGroupProcessor.FeatureSetContentGroups;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.TimeMachine;


public class PaFeaturesProcessorTest {
    
    private static final String PROGRAMME_ID = "progId";
    
    private ContentResolver contentResolver = Mockito.mock(ContentResolver.class);
    private Clock clock = new TimeMachine();

    private final PaFeaturesProcessor processor = new PaFeaturesProcessor(contentResolver);

    @Test
    public void testPaFeaturesAddsToBothGroupsForToday() {
        ResolvedContent value = createItem(PROGRAMME_ID, clock.now(), clock.now().plusMinutes(30));
        
        Mockito.when(contentResolver.findByUris(ImmutableSet.of(PaHelper.getFilmUri(PROGRAMME_ID), 
                PaHelper.getEpisodeUri(PROGRAMME_ID), PaHelper.getAlias(PROGRAMME_ID)))).thenReturn(value);
        
        ContentGroup today = new ContentGroup("today");
        ContentGroup all = new ContentGroup("all");
        
        processor.prepareUpdate(new Interval(clock.now(), clock.now().plusDays(1)));
        processor.process(PROGRAMME_ID, new FeatureSetContentGroups(today, all));

        assertEquals(PROGRAMME_ID, all.getContents().get(0).getUri());
        assertEquals(PROGRAMME_ID, today.getContents().get(0).getUri());
    }

    @Test
    public void testPaFeaturesAddsToOnlyAllGroupForItemInPast() {
        ResolvedContent value = createItem(PROGRAMME_ID, clock.now().minusDays(2), clock.now().minusDays(2).plusMinutes(30));
        
        Mockito.when(contentResolver.findByUris(ImmutableSet.of(PaHelper.getFilmUri(PROGRAMME_ID), 
                PaHelper.getEpisodeUri(PROGRAMME_ID), PaHelper.getAlias(PROGRAMME_ID)))).thenReturn(value);
        
        ContentGroup today = new ContentGroup("today");
        ContentGroup all = new ContentGroup("all");
        
        processor.prepareUpdate(new Interval(clock.now(), clock.now().plusDays(1)));
        processor.process(PROGRAMME_ID, new FeatureSetContentGroups(today, all));

        assertEquals(PROGRAMME_ID, all.getContents().get(0).getUri());
        assertTrue(today.getContents().isEmpty());
    }

    @Test
    public void testPaFeaturesAddsToOnlyAllGroupForItemInFuture() {
        ResolvedContent value = createItem(PROGRAMME_ID, clock.now().plusDays(2), clock.now().plusDays(2).plusMinutes(30));
        
        Mockito.when(contentResolver.findByUris(ImmutableSet.of(PaHelper.getFilmUri(PROGRAMME_ID), 
                PaHelper.getEpisodeUri(PROGRAMME_ID), PaHelper.getAlias(PROGRAMME_ID)))).thenReturn(value);
        
        ContentGroup today = new ContentGroup("today");
        ContentGroup all = new ContentGroup("all");
        
        processor.prepareUpdate(new Interval(clock.now(), clock.now().plusDays(1)));
        processor.process(PROGRAMME_ID, new FeatureSetContentGroups(today, all));

        assertEquals(PROGRAMME_ID, all.getContents().get(0).getUri());
        assertTrue(today.getContents().isEmpty());
    }

    private ResolvedContent createItem(String programmeId, DateTime start, DateTime end) {
        Item item = new Item(programmeId, "curie", Publisher.PA_FEATURES);
        Version version = new Version();
        Broadcast broadcast = new Broadcast("someChannel", start, end);
        version.addBroadcast(broadcast);
        item.setVersions(ImmutableSet.of(version));
        ResolvedContent value = ResolvedContent.builder()
                .put(programmeId, item)
                .build();
        return value;
    }
}
