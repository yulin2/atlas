package org.atlasapi.query.content.schedule;

import junit.framework.TestCase;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.content.Broadcast;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.MediaType;
import org.atlasapi.media.content.MongoContentResolver;
import org.atlasapi.media.content.MongoContentWriter;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.media.content.ResolvedContent;
import org.atlasapi.media.content.Version;
import org.atlasapi.media.content.util.LookupResolvingContentResolver;
import org.atlasapi.persistence.lookup.MongoLookupEntryStore;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.SystemClock;

public class BroadcastRemovingScheduleOverlapListenerTest extends TestCase {

    private DatabasedMongo db;
    private ContentWriter writer;
    private ContentResolver resolver;
    
    private BroadcastRemovingScheduleOverlapListener listener;
    
    private final DateTime now = new DateTime(DateTimeZones.UTC);
    private final Item item = new Item("item1", "item1", Publisher.BBC);
    
    private static final Channel BBC_ONE = new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", MediaType.AUDIO, "http://www.bbc.co.uk/bbcone");

    private final Broadcast broadcast1 = new Broadcast(BBC_ONE.uri(), now, now.plusMinutes(2));
    private final Broadcast broadcast2 = new Broadcast(BBC_ONE.uri(), now.plusMinutes(2), now.plusMinutes(3));
    private final Broadcast broadcast3 = new Broadcast(BBC_ONE.uri(), now.plusMinutes(3), now.plusMinutes(6));
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        db = MongoTestHelper.anEmptyTestDatabase();
        MongoLookupEntryStore lookupWriter = new MongoLookupEntryStore(db);
        writer = new MongoContentWriter(db, lookupWriter , new SystemClock());
        resolver = new LookupResolvingContentResolver(new MongoContentResolver(db), lookupWriter);
        
        Version version = new Version();
        version.addBroadcast(broadcast1);
        version.addBroadcast(broadcast2);
        version.addBroadcast(broadcast3);
        
        item.addVersion(version);
        
        writer.createOrUpdate(item);
        listener = new BroadcastRemovingScheduleOverlapListener(resolver, writer);
    }

    @Test
    public void testRemoveBroadcast() {
        listener.itemRemovedFromSchedule(item, broadcast1);
        
        ResolvedContent result =  resolver.findByCanonicalUris(ImmutableList.of(item.getCanonicalUri()));
        Version v = Iterables.getOnlyElement(((Item)result.get(item.getCanonicalUri()).requireValue()).getVersions());
        assertEquals(v.getBroadcasts(), ImmutableSet.of(broadcast2, broadcast3));
        
        listener.itemRemovedFromSchedule(item, broadcast2);
        
        result = resolver.findByCanonicalUris(ImmutableList.of(item.getCanonicalUri()));
        v = Iterables.getOnlyElement(((Item)result.get(item.getCanonicalUri()).requireValue()).getVersions());
        assertEquals(v.getBroadcasts(), ImmutableSet.of(broadcast3));
    }
}
