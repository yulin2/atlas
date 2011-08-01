package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.remotesite.bbc.ion.BbcIonDateRangeScheduleUpdater.SCHEDULE_PATTERN;
import static org.hamcrest.core.AllOf.allOf;
import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.persistence.testing.StubContentResolver;
import org.atlasapi.remotesite.FixedResponseHttpClient;
import org.atlasapi.remotesite.bbc.BbcIonScheduleClient;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;

public class BbcIonScheduleUpdaterTest extends TestCase {

    private static final String THE_SERVICE = "the_service";
    private static final String THE_DAY = "21010101";
    private static final String ION_FEED_URI = String.format(SCHEDULE_PATTERN, THE_SERVICE, THE_DAY);
    
	private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
	private static final String ITEM_A = SLASH_PROGRAMMES_ROOT + "b00y377q";
    
    private Mockery context = new Mockery();
    
    private final ContentWriter writer = context.mock(ContentWriter.class);
    private final AdapterLog log = new SystemOutAdapterLog(); 
    
    @SuppressWarnings("unchecked")
    public void testProcessNewItemWithNoBrandOrSeries() throws Exception {

        ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
        BbcIonScheduleClient client = new BbcIonScheduleClient(SCHEDULE_PATTERN, FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-no-brand-no-series.json")));
        
        context.checking(new Expectations(){{
            one(writer).createOrUpdate((Item)with(allOf(
                    uri(ITEM_A),
                    title("Pleasure and Pain with Michael Mosley"),
                    version(uri(SLASH_PROGRAMMES_ROOT+"b00y3770")))));
        }});

        BbcIonScheduleHandler handler = new DefaultBbcIonScheduleHandler(resolver, writer, log);
        new BbcIonScheduleUpdateTask(THE_SERVICE,ISODateTimeFormat.basicDate().parseDateTime(THE_DAY), client, handler, log).call();
    }
    
    @SuppressWarnings("unchecked")
    public void testProcessNewEpisodeWithBrandNoSeries() throws Exception {

    	final String item1 = SLASH_PROGRAMMES_ROOT + "b00y1w9h";
    	final String item2 = SLASH_PROGRAMMES_ROOT + "b006m86d";
    	
        ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
        BbcIonScheduleClient client = new BbcIonScheduleClient(SCHEDULE_PATTERN, FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-brand-no-series.json")));

        context.checking(new Expectations(){{
            one(writer).createOrUpdate((Item)with(allOf(
                    uri(item1),
                    title("28/01/2011"),
                    version(uri(SLASH_PROGRAMMES_ROOT+"b00y1w7k"))
            )));
            one(writer).createOrUpdate((Brand) with(allOf(
                    uri(item2)
            )));
        }});

        BbcIonScheduleHandler handler = new DefaultBbcIonScheduleHandler(resolver, writer, log);
        new BbcIonScheduleUpdateTask(THE_SERVICE,ISODateTimeFormat.basicDate().parseDateTime(THE_DAY), client, handler, log).call();
    }

    @SuppressWarnings("unchecked")
    public void testProcessNewEpisodeWithBrandAndSeries() throws Exception {
        ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
        BbcIonScheduleClient client = new BbcIonScheduleClient(SCHEDULE_PATTERN, FixedResponseHttpClient.respondTo(ION_FEED_URI, Resources.getResource("ion-item-brand-series.json")));

        context.checking(new Expectations(){{
            one(writer).createOrUpdate((Item)with(allOf(
                    uri(SLASH_PROGRAMMES_ROOT+"b00y439c"),
                    title("Episode 4"),
                    version(uri(SLASH_PROGRAMMES_ROOT+"b00y4336"))
            )));
            one(writer).createOrUpdate((Brand)with(allOf(
                    uri(SLASH_PROGRAMMES_ROOT+"b00xb44r")
            )));
            one(writer).createOrUpdate((Brand)with(allOf(
                    uri(SLASH_PROGRAMMES_ROOT+"b007gf9k")
            )));
        }});

        BbcIonScheduleHandler handler = new DefaultBbcIonScheduleHandler(resolver, writer, log);
        new BbcIonScheduleUpdateTask(THE_SERVICE,ISODateTimeFormat.basicDate().parseDateTime(THE_DAY), client, handler, log).call();
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
