package org.atlasapi.remotesite.tvblob;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.EventFiringContentWriter;
import org.atlasapi.persistence.content.mongo.MongoDBQueryExecutor;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.content.mongo.MongoScheduleStore;
import org.atlasapi.persistence.content.mongo.ScheduleUpdatingContentListener;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.google.inject.internal.Lists;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.DateTimeZones;

public class TVBlobDayPopulatorTest extends TestCase {

    private MongoDbBackedContentStore store;
    private MongoScheduleStore scheduleStore;
    private TVBlobDayPopulator extractor;
    private EventFiringContentWriter writer;
    private final DateTime now = new DateTime(DateTimeZones.UTC);
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabasedMongo db = MongoTestHelper.anEmptyTestDatabase();
        this.scheduleStore = new MongoScheduleStore(db);
        this.store = new MongoDbBackedContentStore(db);
        this.writer = new EventFiringContentWriter(store, new ScheduleUpdatingContentListener(scheduleStore));
        extractor = new TVBlobDayPopulator(writer, store, "raiuno");
    }
    
    public void testShouldRetrievePlaylistAndItems() throws Exception {
        InputStream is = Resources.getResource(getClass(), "today.json").openStream();
        
        extractor.populate(is);
        
        scheduleStore.schedule(now.minusYears(5), now, ImmutableSet.of(Channel.), ImmutableSet.of(Publisher.TVBLOB));
        ContentQuery query = ContentQueryBuilder.query().equalTo(Attributes.BROADCAST_ON, "http://tvblob.com/channel/raiuno").build();
        
        boolean foundMoreThanOneBroadcast = false;
        boolean foundBrandWithMoreThanOneEpisode = false;
        List<String> brandUris = Lists.newArrayList();
        
        for (Episode episode: Iterables.filter(new MongoDBQueryExecutor(store).discover(query), Episode.class)) {
            if (episode.getContainer() != null) {
                assertNotNull(episode.getContainer().getCanonicalUri());
                if (brandUris.contains(episode.getContainer().getCanonicalUri())) {
                    foundBrandWithMoreThanOneEpisode = true;
                } else {
                    brandUris.add(episode.getContainer().getCanonicalUri());
                }
            }
            assertNotNull(episode.getCanonicalUri());
            assertFalse(episode.getVersions().isEmpty());
            Version version = episode.getVersions().iterator().next();
            if (version.getBroadcasts().size() > 1) {
                foundMoreThanOneBroadcast = true;
                assertEquals(2, version.getBroadcasts().size());
            }
            
            for (Broadcast broadcast: version.getBroadcasts()) {
                assertEquals("http://tvblob.com/channel/raiuno", broadcast.getBroadcastOn());
                assertNotNull(broadcast.getTransmissionTime());
            }
        }
        
        assertTrue(foundMoreThanOneBroadcast);
        assertTrue(foundBrandWithMoreThanOneEpisode);
        
        List<Identified> brands = store.findByCanonicalUri(ImmutableList.of("http://tvblob.com/brand/269"));
        
        assertFalse(brands.isEmpty());
        assertEquals(1, brands.size());
        Brand brand = (Brand) brands.get(0);
        assertEquals(2, brand.getContents().size());
    }
}
