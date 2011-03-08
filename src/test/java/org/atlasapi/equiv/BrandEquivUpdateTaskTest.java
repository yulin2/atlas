package org.atlasapi.equiv;

import static org.atlasapi.media.entity.Channel.BBC_ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.time.DateTimeZones;

public class BrandEquivUpdateTaskTest extends TestCase {
    
    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4);
    private final Mockery context = new Mockery();
    private final ScheduleResolver scheduleResolver = context.mock(ScheduleResolver.class);

    // PA Brand with single item and broadcast
    // BBC brand with single item and identical broadcast
    // Brands are equivalent.
    public void testBasicEquivalence() throws Exception {

        Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectIem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000))));
        
        Brand equivBrand = new Brand("equivBrandUri", "equivCurie", Publisher.BBC);
        final Episode equivEpisode = episodeWithBroadcasts("equivItem", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        equivBrand.addContents(equivEpisode);
        
        context.checking(new Expectations(){{
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(equivEpisode)), interval(40000, 260000))));
        }});
        
        List<Brand> equivalizedBrands = new BrandEquivUpdateTask(subjectBrand, scheduleResolver).call();
        
        assertThat(equivalizedBrands.size(), is(2));
        assertThat(equivalizedBrands.get(0).getEquivalentTo(), hasItem(equivalizedBrands.get(1).getCanonicalUri()));
        assertThat(equivalizedBrands.get(1).getEquivalentTo(), hasItem(equivalizedBrands.get(0).getCanonicalUri()));
        
    }

    //PA brand, 1 item, 1 broadcast
    //BBC brand, 1 item, different broadcast
    //Not equivalent. 
    public void testBasicNonEquivalence() throws Exception {

        Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectIem", Publisher.PA, new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000))));
        
        Brand notEquivBrand = new Brand("notEquivBrandUri", "notEquivCurie", Publisher.BBC);
        final Episode notEquivEpisode = episodeWithBroadcasts("notEquivItem", Publisher.BBC, new Broadcast(Channel.BBC_ONE.uri(), utcTime(120000), utcTime(130000)));
        notEquivBrand.addContents(notEquivEpisode);
        
        context.checking(new Expectations(){{
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode)), interval(40000, 260000))));
        }});
        
        List<Brand> equivalizedBrands = new BrandEquivUpdateTask(subjectBrand, scheduleResolver).call();
        
        assertThat(equivalizedBrands.size(), is(0));
    }
    
    //PA brand, 1 item, 2 broadcasts
    //BBC brand, 1 item, 1 identical broadcast
    public void testEquivalenceWhenMissingBroadcast() throws Exception {

        Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectIem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000))
        ));
        
        Brand equivBrand = new Brand("equivBrandUri", "equivCurie", Publisher.BBC);
        final Episode equivEpisode = episodeWithBroadcasts("equivItem", Publisher.BBC, new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        equivBrand.addContents(equivEpisode);
        
        context.checking(new Expectations(){{
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(equivEpisode)), interval(40000, 260000))));
                
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of()), interval(240000, 460000))));
        }});
        
        List<Brand> equivalizedBrands = new BrandEquivUpdateTask(subjectBrand, scheduleResolver).call();
        
        assertThat(equivalizedBrands.size(), is(2));
        assertThat(equivalizedBrands.get(0).getEquivalentTo(), hasItem(equivalizedBrands.get(1).getCanonicalUri()));
        assertThat(equivalizedBrands.get(1).getEquivalentTo(), hasItem(equivalizedBrands.get(0).getCanonicalUri()));
    }
    
    //PA brand, 1 item, 2 broadcasts
    //BBC brand, 2 item with 1 identical broadcast each
    public void testNotEquivalentWhenItemsDifferent() throws Exception {

        Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectIem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000))
        ));
        
        //Shouldn't matter they're in the same brand.
        Brand notEquivBrand = new Brand("notEquivBrandUri", "notEquivCurie", Publisher.BBC);
        final Episode notEquivEpisode1 = episodeWithBroadcasts("notEquivItem1", Publisher.BBC, new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        final Episode notEquivEpisode2 = episodeWithBroadcasts("notEquivItem2", Publisher.BBC, new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000)));
        notEquivBrand.addContents(notEquivEpisode1, notEquivEpisode2);
        
        context.checking(new Expectations(){{
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS); 
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode1)), interval(40000, 260000))));
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode2)), interval(240000, 460000))));
        }});
        
        List<Brand> equivalizedBrands = new BrandEquivUpdateTask(subjectBrand, scheduleResolver).call();
        
        assertThat(equivalizedBrands.size(), is(0));
    }
    
    //PA brand, 1 item, 3 broadcasts
    //BBC brand 1, 1 item with 2 identical broadcasts
    //BBC brand 2, 1 item with 1 identical broadcasts
    //N.B. Certainty threshold is at 0.65
    public void testEquivalenceWithMajority() throws Exception {

        Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectIem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000)),
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(700000), utcTime(800000))
        ));
        
        /* This has to have two separate but equivalent (according to equals()) 
         * eps because the Schedule sfm would shred a single eps broadcasts.
         */
        Brand equivBrand = new Brand("equivBrandUri", "equivCurie", Publisher.BBC);
        final Episode equivEpisode1 = episodeWithBroadcasts("equivItem1", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        final Episode equivEpisode2 = episodeWithBroadcasts("equivItem1", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(700000), utcTime(800000)));
        equivEpisode2.setContainer(equivBrand);
        equivEpisode1.setContainer(equivBrand);
        
        Brand notEquivBrand = new Brand("notEquivBrandUri", "notEquivCurie", Publisher.BBC);
        final Episode notEquivEpisode = episodeWithBroadcasts("notEquivItem", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000)));
        notEquivBrand.addContents(notEquivEpisode);
        
        context.checking(new Expectations(){{
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(equivEpisode1)), interval(40000, 260000))));
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode)), interval(240000, 460000))));
            one(scheduleResolver).schedule(utcTime(640000), utcTime(860000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(equivEpisode2)), interval(640000, 860000))));
        }});
        
        List<Brand> equivalizedBrands = new BrandEquivUpdateTask(subjectBrand, scheduleResolver).withCertaintyThreshold(0.65).call();
        
        assertThat(equivalizedBrands.size(), is(2));
        
        assertThat(equivalizedBrands.get(0), is(equalTo(subjectBrand)));
        assertThat(equivalizedBrands.get(0).getEquivalentTo().size(), is(equalTo(1)));
        assertThat(equivalizedBrands.get(0).getEquivalentTo(), hasItem(equivBrand.getCanonicalUri()));

        assertThat(equivalizedBrands.get(1), is(equalTo(equivBrand)));
        assertThat(equivalizedBrands.get(1).getEquivalentTo(), hasItem(subjectBrand.getCanonicalUri()));
    }
    
    //PA brand, 1 item, 2 broadcasts
    //BBC brand 1, 1 item with 1 identical broadcasts
    //BBC brand 2, 1 item with 1 identical broadcasts
    public void testNotEquivalentWhenNoMajorityBrand() throws Exception {

        Brand subjectBrand = new Brand("subjectUri", "subjectCurie", Publisher.PA);
        subjectBrand.addContents(episodeWithBroadcasts("subjectIem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000))
        ));
        
        Brand equivBrand = new Brand("notEquivBrandUri1", "notEquivCurie1", Publisher.BBC);
        final Episode notEquivEpisode1 = episodeWithBroadcasts("notEquivItem1", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        equivBrand.addContents(notEquivEpisode1);
        
        Brand notEquivBrand = new Brand("notEquivBrandUri2", "notEquivCurie2", Publisher.BBC);
        final Episode notEquivEpisode2 = episodeWithBroadcasts("notEquivItem2", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(300000), utcTime(400000)));
        notEquivBrand.addContents(notEquivEpisode2);
        
        context.checking(new Expectations(){{
            one(scheduleResolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode1)), interval(40000, 260000))));
            one(scheduleResolver).schedule(utcTime(240000), utcTime(460000), ImmutableSet.of(BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(notEquivEpisode2)), interval(240000, 460000))));
        }});
        
        List<Brand> equivalizedBrands = new BrandEquivUpdateTask(subjectBrand, scheduleResolver).call();
        
        assertThat(equivalizedBrands.size(), is(0));
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
        for (Broadcast broadcast : broadcasts) {
            version.addBroadcast(broadcast);
        }
        item.addVersion(version);
        return item;
    }

}
