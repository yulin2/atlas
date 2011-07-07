package org.atlasapi.equiv.generators;

import static org.atlasapi.media.entity.Channel.BBC_ONE;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.joda.time.Duration.standardMinutes;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.time.DateTimeZones;

public class BroadcastMatchingItemEquivalenceGeneratorTest extends MockObjectTestCase {
    
    private final ScheduleResolver resolver = mock(ScheduleResolver.class);
    private final BroadcastMatchingItemEquivalenceGenerator generator = new BroadcastMatchingItemEquivalenceGenerator(resolver , ImmutableSet.of(BBC), standardMinutes(1));
    
    public void testGenerateEquivalencesForOneMatchingBroadcast() {
        final Item item1 = episodeWithBroadcasts("subjectItem", Publisher.PA, 
                new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)),
                new Broadcast(Channel.BBC_ONE_CAMBRIDGE.uri(), utcTime(100000), utcTime(200000)));//ignored
        
        final Item item2 = episodeWithBroadcasts("equivItem", Publisher.BBC, new Broadcast(Channel.BBC_ONE.uri(), utcTime(100000), utcTime(200000)));
        
        checking(new Expectations(){{
            one(resolver).schedule(utcTime(40000), utcTime(260000), ImmutableSet.of(Channel.BBC_ONE), ImmutableSet.of(BBC));
                will(returnValue(Schedule.fromChannelMap(ImmutableMap.of(BBC_ONE, (List<Item>)ImmutableList.<Item>of(item2)), interval(40000, 260000))));
        }});
        
        ScoredEquivalents<Item> equivalents = generator.generate(item1);
        
        Map<Item, Score> scoreMap = equivalents.equivalents();
        
        assertThat(scoreMap.size(), is(1));
        assertThat(scoreMap.get(item2).asDouble(), is(equalTo(1.0)));
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
