package org.atlasapi.query.content.schedule;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.DateTimeZones;

public class BroadcastRemovingScheduleOverlapListenerTest extends TestCase {

    private DatabasedMongo db;
    private MongoDbBackedContentStore store;
    
    private BroadcastRemovingScheduleOverlapListener listener;
    
    private final DateTime now = new DateTime(DateTimeZones.UTC);
    private final Item item = new Item("item1", "item1", Publisher.BBC);
    
    private final Broadcast broadcast1 = new Broadcast(Channel.BBC_ONE.uri(), now, now.plusMinutes(2));
    private final Broadcast broadcast2 = new Broadcast(Channel.BBC_ONE.uri(), now.plusMinutes(2), now.plusMinutes(3));
    private final Broadcast broadcast3 = new Broadcast(Channel.BBC_ONE.uri(), now.plusMinutes(3), now.plusMinutes(6));
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = MongoTestHelper.anEmptyTestDatabase();
        store = new MongoDbBackedContentStore(db);
        
        Version version = new Version();
        version.addBroadcast(broadcast1);
        version.addBroadcast(broadcast2);
        version.addBroadcast(broadcast3);
        
        item.addVersion(version);
        
        store.createOrUpdate(item);
        listener = new BroadcastRemovingScheduleOverlapListener(store, store);
    }
    
    public void testRemoveBroadcast() {
        listener.itemRemovedFromSchedule(item, broadcast1);
        
        Item result = (Item) store.findByCanonicalUri(item.getCanonicalUri());
        Version v = Iterables.getOnlyElement(result.getVersions());
        assertEquals(v.getBroadcasts(), ImmutableSet.of(broadcast2, broadcast3));
        
        listener.itemRemovedFromSchedule(item, broadcast2);
        
        result = (Item) store.findByCanonicalUri(item.getCanonicalUri());
        v = Iterables.getOnlyElement(result.getVersions());
        assertEquals(v.getBroadcasts(), ImmutableSet.of(broadcast3));
    }
}
