package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.remotesite.bbc.BbcFeeds.slashProgrammesUriForPid;
import static org.atlasapi.remotesite.bbc.BbcModule.SCHEDULE_DEFAULT_FORMAT;
import static org.atlasapi.remotesite.bbc.ion.HttpBackedBbcIonClient.ionClient;
import static org.hamcrest.core.AllOf.allOf;
import junit.framework.TestCase;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.FixedResponseHttpClient;
import org.atlasapi.remotesite.bbc.ContentLock;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTimeZone;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonScheduleUpdaterTest extends TestCase {

    private static final String SERVICE = "the_service";
    private static final String DAY = "21010101";
    private static final String ION_FEED_URI = String.format(SCHEDULE_DEFAULT_FORMAT, SERVICE, DAY);
    
    private Mockery context = new Mockery();
    
    private final ContentStore store = context.mock(ContentStore.class);
    private final ChannelResolver channelResolver = context.mock(ChannelResolver.class);
    private final AdapterLog log = new SystemOutAdapterLog(); 
    private final Channel channel = Channel.builder().build();
    private final BbcIonBroadcastHandler handler = new DefaultBbcIonBroadcastHandler(store, log, new ContentLock());
    
    protected void setUp() {
        DateTimeZone.setDefault(DateTimeZones.UTC);
    }

    @SuppressWarnings("unchecked")
    public void testProcessNewItemWithNoBrandOrSeries() throws Exception {

        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-no-brand-no-series.json")),IonSchedule.class);
        
        context.checking(new Expectations(){{
            one(store).resolveAliases(with(any(Iterable.class)),with(Publisher.BBC));
                will(returnValue(ImmutableOptionalMap.fromMap(ImmutableMap.of())));
            one(store).writeContent((Item)with(allOf(
                    uri(slashProgrammesUriForPid("b00y377q")),
                    title("Pleasure and Pain with Michael Mosley"),
                    version(uri(slashProgrammesUriForPid("b00y3770"))))));
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london");
                will(returnValue(Maybe.just(channel)));
        }});

        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, channelResolver, log).call();
    }
    
    @SuppressWarnings("unchecked")
    public void testProcessNewEpisodeWithBrandNoSeries() throws Exception {

    	final String item1 = slashProgrammesUriForPid("b00y1w9h");
    	final String item2 = slashProgrammesUriForPid("b006m86d");
    	
        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-brand-no-series.json")), IonSchedule.class);

        context.checking(new Expectations(){{            
            allowing(store).resolveAliases(with(any(Iterable.class)), with(Publisher.BBC));
                will(returnValue(ImmutableOptionalMap.fromMap(ImmutableMap.of())));
            one(store).writeContent((Item)with(allOf(
                    uri(item1),
                    title("28/01/2011"),
                    version(uri(slashProgrammesUriForPid("b00y1w7k")))
            )));
            one(store).writeContent((Brand) with(allOf(
                    uri(item2)
            )));
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london"); will(returnValue(Maybe.just(channel)));
        }});

        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, channelResolver, log).call();
    }

    @SuppressWarnings("unchecked")
    public void testProcessNewEpisodeWithBrandAndSeries() throws Exception {
        RemoteSiteClient<IonSchedule> client = ionClient(FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-brand-series.json")), IonSchedule.class);

        context.checking(new Expectations(){{
            allowing(store).resolveAliases(with(any(Iterable.class)), with(BBC));
                will(returnValue(ImmutableOptionalMap.fromMap(ImmutableMap.of())));
            one(store).writeContent((Item)with(allOf(
                    uri(slashProgrammesUriForPid("b00y439c")),
                    title("Episode 4"),
                    version(uri(slashProgrammesUriForPid("b00y4336")))
            )));
            one(store).writeContent((Brand)with(allOf(
                    uri(slashProgrammesUriForPid("b00xb44r"))
            )));
            one(store).writeContent((Brand)with(allOf(
                    uri(slashProgrammesUriForPid("b007gf9k"))
            )));
            one(channelResolver).fromUri("http://www.bbc.co.uk/services/bbcone/london"); will(returnValue(Maybe.just(channel)));
        }});

        new BbcIonScheduleUpdateTask(ION_FEED_URI, client, handler, channelResolver, log).call();
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
