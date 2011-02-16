package org.atlasapi.remotesite.channel4.epg;

import static org.atlasapi.media.entity.Channel.CHANNEL_FOUR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class BroadcastTrimmerTest extends TestCase {

    public void testTrimBroadcasts() {

        final KnownTypeQueryExecutor queryExecutor = queryExecutor();
        final ContentWriter contentWriter = contentWriter();
        
        AdapterLog log = new NullAdapterLog();
        
        BroadcastTrimmer trimmer = new BroadcastTrimmer(Publisher.C4, queryExecutor, contentWriter, log);
        
        Interval scheduleInterval = new Interval(100, 200);
        trimmer.trimBroadcasts(scheduleInterval, CHANNEL_FOUR, ImmutableSet.of("c4:1234"));
        
    }

    private ContentWriter contentWriter() {
        return new ContentWriter() {
            
            @Override
            public void createOrUpdateSkeleton(ContentGroup playlist) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public void createOrUpdate(Container<?> container, boolean markMissingItemsAsUnavailable) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public void createOrUpdate(Item item) {
                checkItem(item);
            }

        };
    }

    private void checkItem(Item item) {
        Set<Broadcast> broadcasts = Iterables.getOnlyElement(item.getVersions()).getBroadcasts();
        assertThat(broadcasts.size(), is(equalTo(1)));
        assertThat(Iterables.get(broadcasts, 0).getId(), is(equalTo("c4:1234")));
    }
    
    private KnownTypeQueryExecutor queryExecutor() {
        return new KnownTypeQueryExecutor() {
            
            @Override
            public Schedule schedule(ContentQuery query) {
                return Schedule.fromItems(new Interval(100, 200), buildItems());
            }

            @Override
            public List<Identified> executeUriQuery(Iterable<String> uris, ContentQuery query) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public List<Content> discover(ContentQuery query) {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    private Iterable<? extends Item> buildItems() {
        Item item = new Item("testUri", "testCurie", Publisher.C4);
        Version version = new Version();
        
        Broadcast retain = new Broadcast(Channel.CHANNEL_FOUR.uri(), new DateTime(105), new DateTime(120)).withId("c4:1234");
        Broadcast remove = new Broadcast(Channel.CHANNEL_FOUR.uri(), new DateTime(150), new DateTime(165)).withId("c4:2234");
        
        version.setBroadcasts(ImmutableSet.of(retain, remove));
        item.addVersion(version);
        
        return ImmutableSet.of(item);
    }
}
