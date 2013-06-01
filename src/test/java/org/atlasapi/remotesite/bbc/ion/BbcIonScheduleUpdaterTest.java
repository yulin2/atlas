package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.remotesite.bbc.BbcFeeds.slashProgrammesUriForPid;
import static org.atlasapi.remotesite.bbc.BbcModule.SCHEDULE_DEFAULT_FORMAT;
import static org.atlasapi.remotesite.bbc.ion.HttpBackedBbcIonClient.ionClient;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.AllOf.allOf;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.FixedResponseHttpClient;
import org.atlasapi.remotesite.bbc.ContentLock;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(JMock.class)
public class BbcIonScheduleUpdaterTest {

    private static final String SERVICE = "the_service";
    private static final String DAY = "21010101";
    private static final String ION_FEED_URI = String.format(SCHEDULE_DEFAULT_FORMAT, SERVICE, DAY);
    
    private Mockery context = new Mockery();
    
    private final ContentWriter writer = context.mock(ContentWriter.class);
    private final ContentResolver resolver = context.mock(ContentResolver.class);
    private final BroadcastTrimmer trimmer = context.mock(BroadcastTrimmer.class);
    private final ChannelResolver channelResolver = context.mock(ChannelResolver.class);
    private final ScheduleWriter scheduleWriter = context.mock(ScheduleWriter.class);
    private final AdapterLog log = new SystemOutAdapterLog(); 
    private final Channel channel = Channel.builder().build();
    private final BbcIonBroadcastHandler handler = new DefaultBbcIonBroadcastHandler(resolver, writer, log, new ContentLock());
    
    @Before
    public void setUp() {
        DateTimeZone.setDefault(DateTimeZones.UTC);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessNewItemWithNoBrandOrSeries() throws Exception {

        final Broadcast broadcast = new Broadcast("http://www.bbc.co.uk/services/bbcone/london", 
                new DateTime(2011, DateTimeConstants.JANUARY, 25, 22, 35, 0, 0),
                new DateTime(2011, DateTimeConstants.JANUARY, 25, 23, 35, 0, 0),
                null);
        
        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-no-brand-no-series.json")),IonSchedule.class);
        
        context.checking(new Expectations(){{
            one(resolver).findByCanonicalUris((Iterable<String>)with(anything()));will(returnValue(ResolvedContent.builder().build()));
            one(writer).createOrUpdate((Item)with(allOf(
                    uri(slashProgrammesUriForPid("b00y377q")),
                    title("Pleasure and Pain with Michael Mosley"),
                    version(uri(slashProgrammesUriForPid("b00y3770"))))));
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london"); will(returnValue(Maybe.just(channel)));
            one(trimmer).trimBroadcasts(
                    new Interval(new DateTime(2011, DateTimeConstants.JANUARY, 25, 22, 35, 0, 0), new DateTime(2011, DateTimeConstants.JANUARY, 25, 23, 35, 0, 0)), 
                    channel, 
                    ImmutableMap.<String, String>of("bbc:p00dbbvg", slashProgrammesUriForPid("b00y377q")));
            
            one(scheduleWriter).replaceScheduleBlock(with(Publisher.BBC), with(channel), 
                    with(hasItems(new ItemRefAndBroadcast(slashProgrammesUriForPid("b00y377q"), broadcast))));
        }});

        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, trimmer, channelResolver, scheduleWriter, log).call();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProcessNewEpisodeWithBrandNoSeries() throws Exception {

        final Broadcast expectedBroadcast = new Broadcast("http://www.bbc.co.uk/services/bbcone/london", 
                new DateTime(2011, DateTimeConstants.JANUARY, 28, 20, 00, 0, 0),
                new DateTime(2011, DateTimeConstants.JANUARY, 28, 20, 30, 0, 0),
                null);
        
    	final String item1 = slashProgrammesUriForPid("b00y1w9h");
    	final String item2 = slashProgrammesUriForPid("b006m86d");
    	
        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-brand-no-series.json")), IonSchedule.class);

        context.checking(new Expectations(){{            
            allowing(resolver).findByCanonicalUris((Iterable<String>)with(anything()));will(returnValue(ResolvedContent.builder().build()));
            one(writer).createOrUpdate((Item)with(allOf(
                    uri(item1),
                    title("28/01/2011"),
                    version(uri(slashProgrammesUriForPid("b00y1w7k")))
            )));
            one(writer).createOrUpdate((Brand) with(allOf(
                    uri(item2)
            )));
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london"); will(returnValue(Maybe.just(channel)));
            one(trimmer).trimBroadcasts(
                    new Interval(new DateTime(2011, DateTimeConstants.JANUARY, 28, 20, 00, 0, 0), new DateTime(2011, DateTimeConstants.JANUARY, 28, 20, 30, 0, 0)), 
                    channel, 
                    ImmutableMap.<String, String>of("bbc:p00dd6dm", slashProgrammesUriForPid("b00y1w9h")));
            one(scheduleWriter).replaceScheduleBlock(with(Publisher.BBC), with(channel), 
                    with(hasItems(new ItemRefAndBroadcast(slashProgrammesUriForPid("b00y1w9h"), expectedBroadcast))));
        }});

        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, trimmer, channelResolver, scheduleWriter, log).call();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessNewEpisodeWithBrandAndSeries() throws Exception {
        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-brand-series.json")), IonSchedule.class);

        final Broadcast expectedBroadcast = new Broadcast("http://www.bbc.co.uk/services/bbcone/london", 
                new DateTime(2011, DateTimeConstants.JANUARY, 28, 21, 00, 0, 0),
                new DateTime(2011, DateTimeConstants.JANUARY, 28, 22, 00, 0, 0),
                null);
        
        context.checking(new Expectations(){{
            allowing(resolver).findByCanonicalUris((Iterable<String>)with(anything()));will(returnValue(ResolvedContent.builder().build()));
            one(writer).createOrUpdate((Item)with(allOf(
                    uri(slashProgrammesUriForPid("b00y439c")),
                    title("Episode 4"),
                    version(uri(slashProgrammesUriForPid("b00y4336")))
            )));
            one(writer).createOrUpdate((Brand)with(allOf(
                    uri(slashProgrammesUriForPid("b00xb44r"))
            )));
            one(writer).createOrUpdate((Brand)with(allOf(
                    uri(slashProgrammesUriForPid("b007gf9k"))
            )));
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london"); will(returnValue(Maybe.just(channel)));
            one(trimmer).trimBroadcasts(
                    new Interval(new DateTime(2011, DateTimeConstants.JANUARY, 28, 21, 00, 0, 0), new DateTime(2011, DateTimeConstants.JANUARY, 28, 22, 00, 0, 0)), 
                    channel, 
                    ImmutableMap.<String, String>of("bbc:p00dd6dp", slashProgrammesUriForPid("b00y439c")));
            one(scheduleWriter).replaceScheduleBlock(with(Publisher.BBC), with(channel), 
                    with(hasItems(new ItemRefAndBroadcast(slashProgrammesUriForPid("b00y439c"), expectedBroadcast))));
        }});

        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, trimmer, channelResolver, scheduleWriter, log).call();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testScheduleWritingWithMultipleItemsInSchedule() throws Exception {
        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-schedule-multiple-items.json")), IonSchedule.class);
        
        final Broadcast item1ExpectedBroadcast = new Broadcast("http://www.bbc.co.uk/services/bbcone/london", 
                new DateTime(2013, DateTimeConstants.JUNE, 1, 5, 00, 0, 0),
                new DateTime(2013, DateTimeConstants.JUNE, 1, 9, 00, 0, 0),
                null);
        
        final Broadcast item2ExpectedBroadcast = new Broadcast("http://www.bbc.co.uk/services/bbcone/london", 
                new DateTime(2013, DateTimeConstants.JUNE, 1, 9, 00, 0, 0),
                new DateTime(2013, DateTimeConstants.JUNE, 1, 10, 30, 0, 0),
                null);
        
        context.checking(new Expectations() {{
            allowing(resolver).findByCanonicalUris((Iterable<String>)with(anything()));will(returnValue(ResolvedContent.builder().build()));
            
            one(writer).createOrUpdate((Brand)with(allOf(uri(slashProgrammesUriForPid("b006v5tb")))));
            one(writer).createOrUpdate((Item) with(allOf(uri(slashProgrammesUriForPid("b0223xhq")))));
            one(writer).createOrUpdate((Brand)with(allOf(uri(slashProgrammesUriForPid("b006v5y2")))));
            one(writer).createOrUpdate((Item) with(allOf(uri(slashProgrammesUriForPid("b02tsmxb")))));
            
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london"); will(returnValue(Maybe.just(channel)));
            one(trimmer).trimBroadcasts(
                    new Interval(
                            new DateTime(2013, DateTimeConstants.JUNE, 1, 05, 00, 0, 0), 
                            new DateTime(2013, DateTimeConstants.JUNE, 1, 10, 30, 0, 0)), 
                    channel, 
                    ImmutableMap.<String, String>of(
                            "bbc:p019hbvh", slashProgrammesUriForPid("b0223xhq"), 
                            "bbc:p019hbwx", slashProgrammesUriForPid("b02tsmxb")));
            
            one(scheduleWriter).replaceScheduleBlock(with(Publisher.BBC), with(channel), 
                    with(hasItems(
                            new ItemRefAndBroadcast(slashProgrammesUriForPid("b0223xhq"), item1ExpectedBroadcast),
                            new ItemRefAndBroadcast(slashProgrammesUriForPid("b02tsmxb"), item2ExpectedBroadcast)
                    )));
            
        }});
        
        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, trimmer, channelResolver, scheduleWriter, log).call();
    }
    
    private Matcher<Item> version(final Matcher<? super Version> versionMatcher) {
        return new FunctionBasedDescriptionMatcher<Item>("item with version " + versionMatcher, new Function<Item,Boolean>() {
            @Override
            public Boolean apply(Item input) {
                return Iterables.any(input.getVersions(), new Predicate<Version>() {
                    @Override
                    public boolean apply(Version input) {
                        return versionMatcher.matches(input);
                    }
                });
            }
        });
    }
    
    private Matcher<Item> title(final String title) {
        return new FunctionBasedDescriptionMatcher<Item>(String.format("item with title '%s'",title), new Function<Item, Boolean>() {
            @Override
            public Boolean apply(Item item) {
                return title.equals(item.getTitle());
            }
        });
    }
    
    private static <T extends Identified> Matcher<T> uri(final String uri) {
        return new FunctionBasedDescriptionMatcher<T>("item with uri " + uri, new Function<T, Boolean>() {
            @Override
            public Boolean apply(T item) {
                return uri.equals(item.getCanonicalUri());
            }
        });
    }
    
    private static class FunctionBasedDescriptionMatcher<T extends Identified> extends TypeSafeMatcher<T> {
        
        private final String desc;
        private final Function<? super T, Boolean> tester;

        public FunctionBasedDescriptionMatcher(String desc, Function<? super T, Boolean> tester) {
            this.desc = desc;
            this.tester = tester;
        }
        
        @Override
        public void describeTo(org.hamcrest.Description desc) {
            desc.appendText(this.desc);
        }

        @Override
        public boolean matchesSafely(T item) {
            return tester.apply(item);
        }
        
    }
}
