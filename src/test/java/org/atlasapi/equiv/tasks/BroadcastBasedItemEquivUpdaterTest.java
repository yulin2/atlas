package org.atlasapi.equiv.tasks;

import static com.google.common.collect.Ordering.usingToString;
import static org.atlasapi.media.entity.Channel.BBC_ONE;
import static org.atlasapi.media.entity.Channel.BBC_TWO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

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
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.time.DateTimeZones;

public class BroadcastBasedItemEquivUpdaterTest extends TestCase {

    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4, Publisher.FIVE);
    
    private final Mockery context = new Mockery();
    private final ScheduleResolver resolver = context.mock(ScheduleResolver.class);
    private final BroadcastBasedItemEquivUpdater updater = new BroadcastBasedItemEquivUpdater(resolver);

    public void testStrongSuggestionsForEquivalentBroadcast() {
        // Items with equivalent broadcasts
        final Item item1 = episodeWithBroadcasts("subjectItem", Publisher.PA, new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        final Item item2 = episodeWithBroadcasts("equivItem", Publisher.BBC, new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        
        context.checking(new Expectations(){{
            one(resolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(Channel.BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(item2)), interval(40000, 260000))));
        }});
        
        EquivResult<Item> result = updater.updateEquivalence(item1);
        
        context.assertIsSatisfied();
        
        assertThat(ImmutableMap.copyOf(result.strongSuggestions()), is(equalTo(ImmutableMap.of(Publisher.BBC, item2))));
    }
    
    @SuppressWarnings("unchecked")
    public void testNoStrongSuggestionsForEquivalentBroadcastsForDifferentItems() {
        // Items with equivalent broadcasts
        final Item item1 = episodeWithBroadcasts("subjectItem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(Channel.BBC_TWO.uri(), utcTime(400000), utcTime(500000))
        );
        final Item item2 = episodeWithBroadcasts("notEquivItem1", Publisher.BBC, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000))
        );
        final Item item3 = episodeWithBroadcasts("notEquivItem2", Publisher.BBC, 
                new Broadcast(Channel.BBC_TWO.uri(), utcTime(400000), utcTime(500000))
        );
        
        context.checking(new Expectations(){{
            one(resolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(Channel.BBC_ONE), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(item2)), interval(40000, 260000))));
            one(resolver).schedule(utcTime(340000), utcTime(560000), ImmutableSet.of(Channel.BBC_TWO), TARGET_PUBLISHERS);
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_TWO, (List<Item>)ImmutableList.<Item>of(item3)), interval(340000, 560000))));
        }});
        
        EquivResult<Item> result = updater.updateEquivalence(item1);
        
        context.assertIsSatisfied();
        
        assertThat(ImmutableMap.copyOf(result.strongSuggestions()), is(equalTo(ImmutableMap.<Publisher,Item>of())));
        assertThat(result.suggestedEquivalents().getBinnedCountedSuggestions().get(Publisher.BBC), 
                hasItems(new Count<Item>(item3, usingToString(), 1), new Count<Item>(item2, usingToString(), 1)));
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
        version.setProvider(publisher);
        for (Broadcast broadcast : broadcasts) {
            version.addBroadcast(broadcast);
        }
        item.addVersion(version);
        return item;
    }
}
