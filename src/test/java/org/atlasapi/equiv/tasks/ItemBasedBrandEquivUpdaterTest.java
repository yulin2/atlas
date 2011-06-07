package org.atlasapi.equiv.tasks;

import static org.atlasapi.media.entity.Channel.BBC_ONE;
import static org.atlasapi.media.entity.Channel.BBC_THREE;
import static org.atlasapi.media.entity.Channel.BBC_TWO;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.metabroadcast.common.time.DateTimeZones;

public class ItemBasedBrandEquivUpdaterTest extends TestCase {
    
    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4, Publisher.FIVE);
    private final Mockery context = new Mockery();
    private final ScheduleResolver scheduleResolver = context.mock(ScheduleResolver.class);
    private final BroadcastMatchingItemEquivGenerator broadcastEquivGenerator = new BroadcastMatchingItemEquivGenerator(scheduleResolver);
    private final DelegatingItemEquivGenerator delegatingEquivGenerator = new DelegatingItemEquivGenerator(ImmutableList.<ItemEquivGenerator>of(broadcastEquivGenerator));
    private final ContentResolver contentResolver = context.mock(ContentResolver.class);

    public void testUpdatesEquivalenceWithABrandAndStandAloneItem() throws Exception {
        // subject brand with three items and some broadcasts 
        final Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectItem1", Publisher.PA, 
                new Broadcast(BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(BBC_TWO.uri(), utcTime(100000), utcTime(200000))
        ));
        subjectBrand.addContents(episodeWithBroadcasts("subjectItem2", Publisher.PA, 
                new Broadcast(BBC_ONE.uri(), utcTime(300000), utcTime(400000)),
                new Broadcast(BBC_TWO.uri(), utcTime(300000), utcTime(400000)),
                new Broadcast(BBC_THREE.uri(), utcTime(300000), utcTime(400000))
        ));
        subjectBrand.addContents(episodeWithBroadcasts("subjectItem3", Publisher.PA, 
                new Broadcast(BBC_ONE.uri(), utcTime(700000), utcTime(800000)),
                new Broadcast(BBC_TWO.uri(), utcTime(700000), utcTime(800000)),
                new Broadcast(BBC_THREE.uri(), utcTime(700000), utcTime(800000))
        ));
        
        // a previously equivalent brand.
        final Brand prevEquivBrand = new Brand("prevEquivUri", "prevEquivCurie", Publisher.C4);
        subjectBrand.addEquivalentTo(prevEquivBrand);
        prevEquivBrand.addEquivalentTo(subjectBrand);
        
        //An equivalent brand
        final Brand equivBrand = new Brand("equivBrandUri", "equivCurie", Publisher.BBC);
        
        final Episode equivEpisode1 = episodeWithBroadcasts("equivItem1", Publisher.BBC, 
                new Broadcast(BBC_ONE.uri(), utcTime(100000), utcTime(200000))
        );
        equivBrand.addContents(equivEpisode1);

        //This item is 'outcompeted' by the stand alone item
        final Episode notEquivEpisode = episodeWithBroadcasts("notEquivItem", Publisher.BBC, 
                new Broadcast(BBC_ONE.uri(), utcTime(300000), utcTime(400000))
        );
        equivBrand.addContents(notEquivEpisode);
        
        final Episode equivEpisode2 = episodeWithBroadcasts("equivItem2", Publisher.BBC, 
                new Broadcast(BBC_ONE.uri(), utcTime(700000), utcTime(800000)),
                new Broadcast(BBC_TWO.uri(), utcTime(700000), utcTime(800000))
        );
        equivBrand.addContents(equivEpisode2);
        
        //An item which is equivalent to a specific episode of the subject brand.
        final Item standaloneItem = new Item("standaloneUri", "standaloneCurie", Publisher.BBC);
        Version version = new Version();
        version.setCanonicalUri("standaloneItemVersion");
        version.setProvider(Publisher.BBC);
        version.addBroadcast(new Broadcast(BBC_TWO.uri(), utcTime(300000), utcTime(400000)));
        version.addBroadcast(new Broadcast(BBC_THREE.uri(), utcTime(300000), utcTime(400000)));
        standaloneItem.addVersion(version);
        
        context.checking(new Expectations(){{
            
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(equivEpisode1)), interval(40000, 260000))));
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_TWO), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_TWO, (List<Item>)ImmutableList.<Item>of()), interval(40000, 260000))));
                
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode)), interval(240000, 460000))));
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_TWO), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_TWO, (List<Item>)ImmutableList.<Item>of(standaloneItem)), interval(240000, 460000))));
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_THREE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_THREE, (List<Item>)ImmutableList.<Item>of(standaloneItem)), interval(240000, 460000))));                
            
            one(scheduleResolver).schedule(utcTime(640000), utcTime(860000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(equivEpisode2)), interval(640000, 860000))));
            one(scheduleResolver).schedule(utcTime(640000), utcTime(860000), ImmutableSet.of(BBC_TWO), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_TWO, (List<Item>)ImmutableList.<Item>of(equivEpisode2)), interval(640000, 860000))));
            one(scheduleResolver).schedule(utcTime(640000), utcTime(860000), ImmutableSet.of(BBC_THREE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_THREE, (List<Item>)ImmutableList.<Item>of()), interval(640000, 860000))));
                
            one(contentResolver).findByCanonicalUri("prevEquivUri"); will(returnValue(prevEquivBrand));
        }});
        
        WriteChecker contentWriter = new WriteChecker();
        
        @SuppressWarnings("unused")
        EquivResult<Container<?>> updateEquivalence = new ItemBasedBrandEquivUpdater(delegatingEquivGenerator, contentResolver, contentWriter).withCertaintyThreshold(0.65).updateEquivalence(subjectBrand);
        
        context.assertIsSatisfied();
        
        contentWriter.checkWrittenContent();
    }
    
    private static class WriteChecker implements ContentWriter {
        
        private final Map<String, Content> writtenContent = Maps.newHashMap();

        public void checkWrittenContent() {
            assertTrue("subject brand not written", writtenContent.containsKey("subjectUri"));
            assertTrue("prev equiv brand not written", writtenContent.containsKey("prevEquivUri"));
            assertTrue("equiv brand not written", writtenContent.containsKey("equivBrandUri"));
            assertTrue("equiv item not written", writtenContent.containsKey("standaloneUri"));
        }

        
        @Override
        public void createOrUpdate(Item item) {
            writtenContent.put(item.getCanonicalUri(), item);
        }
        @Override
        public void createOrUpdate(Container<?> container) {
            writtenContent.put(container.getCanonicalUri(), container);
        }

        @Override
        public void createOrUpdateSkeleton(ContentGroup playlist) {
            throw new UnsupportedOperationException();
        }
        
    }

    private Interval interval(long startMillis, long endMillis) {
        return new Interval(startMillis, endMillis, DateTimeZones.UTC);
    }
    
    private DateTime utcTime(long millis) {
        return new DateTime(millis, DateTimeZones.UTC);
    }
    
    private Episode episodeWithBroadcasts(String episodeId, Publisher publisher, Broadcast... broadcasts) {
        Episode item = new Episode(episodeId+"Uri", episodeId+"Curie", publisher);
        Version version = new Version();
        version.setCanonicalUri(episodeId+"Version");
        for (Broadcast broadcast : broadcasts) {
            version.addBroadcast(broadcast);
        }
        item.addVersion(version);
        return item;
    }

}
