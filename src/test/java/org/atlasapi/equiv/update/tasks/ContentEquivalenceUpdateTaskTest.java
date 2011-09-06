package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_ITEMS;
import static org.atlasapi.persistence.content.listing.ContentListingProgress.progressFor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingHandler;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class ContentEquivalenceUpdateTaskTest extends TestCase {

    private final Mockery context = new Mockery();
    private final @SuppressWarnings("unchecked")
    ContentEquivalenceUpdater<Content> updater = context.mock(ContentEquivalenceUpdater.class);
    private final AdapterLog log = new NullAdapterLog();
    private final ScheduleTaskProgressStore progressStore = context.mock(ScheduleTaskProgressStore.class);

    private final Item paItemOne = new Item("pa1", "pa1c", Publisher.PA);
    private final Item bbcItemOne = new Item("bbc1", "bbc1c", Publisher.BBC);
    private final Item bbcItemTwo = new Item("bbc2", "bbc2c", Publisher.BBC);
    private final Item bbcItemThree = new Item("bbc3", "bbc3c", Publisher.BBC);
    private final Item c4ItemOne = new Item("c41", "c41c", Publisher.C4);

    public void testRequestsForEachPublisher() {

        ContentLister contentLister = new ContentLister() {
            
            private Map<Publisher,List<Content>> contents = ImmutableMap.<Publisher,List<Content>>of(
                    PA, ImmutableList.<Content>of(paItemOne),
                    BBC, ImmutableList.<Content>of(bbcItemOne),
                    C4, ImmutableList.<Content>of(c4ItemOne)
            );
            
            @Override
            public boolean listContent(Set<ContentTable> tables, ContentListingCriteria criteria, ContentListingHandler handler) {
                List<Content> pubContents = contents.get(Iterables.getOnlyElement(criteria.getPublishers()));
                handler.handle(pubContents, ContentListingProgress.progressFor(Iterables.getLast(pubContents), ContentTable.TOP_LEVEL_ITEMS));
                return true;
            }
        };
        
        context.checking(new Expectations(){{
            one(progressStore).progressForTask(with("pressassociation.com-bbc.co.uk-channel4.com-equivalence"));
                will(returnValue(new PublisherListingProgress(ContentListingProgress.START, null)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, PA)));
            one(updater).updateEquivalences(paItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(paItemOne.getCanonicalUri(), PA)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, BBC)));
            one(updater).updateEquivalences(bbcItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(bbcItemOne.getCanonicalUri(), BBC)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, C4)));
            one(updater).updateEquivalences(c4ItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(c4ItemOne.getCanonicalUri(), C4)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, null)));
        }});
        
        new ContentEquivalenceUpdateTask(contentLister, updater, log, progressStore ).forPublishers(PA, BBC, C4).run();
        
        context.assertIsSatisfied();
        
    }
    
    public void testStartsFromRestoredPosition() {
        
        ContentLister contentLister = new ContentLister() {
            
            private Map<Publisher,List<Content>> contents = ImmutableMap.<Publisher,List<Content>>of(
                    PA, ImmutableList.<Content>of(paItemOne),
                    BBC, ImmutableList.<Content>of(bbcItemTwo, bbcItemThree),
                    C4, ImmutableList.<Content>of(c4ItemOne)
            );
            
            @Override
            public boolean listContent(Set<ContentTable> tables, ContentListingCriteria criteria, ContentListingHandler handler) {
                List<Content> pubContents = contents.get(Iterables.getOnlyElement(criteria.getPublishers()));
                handler.handle(pubContents, ContentListingProgress.progressFor(Iterables.getLast(pubContents), TOP_LEVEL_ITEMS));
                return true;
            }
        };
        
        context.checking(new Expectations(){{
            one(progressStore).progressForTask(with("pressassociation.com-bbc.co.uk-channel4.com-equivalence"));
                will(returnValue(new PublisherListingProgress(progressFor(bbcItemTwo, TOP_LEVEL_ITEMS), BBC)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(bbcItemTwo.getCanonicalUri(), BBC)));
            one(updater).updateEquivalences(bbcItemTwo);
            one(updater).updateEquivalences(bbcItemThree);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(bbcItemThree.getCanonicalUri(), BBC)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, C4)));
            one(updater).updateEquivalences(c4ItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(c4ItemOne.getCanonicalUri(), C4)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, null)));
        }});
        
        new ContentEquivalenceUpdateTask(contentLister, updater, log, progressStore ).forPublishers(PA, BBC, C4).run();
        
        context.assertIsSatisfied();
        
    }
    
    public void testStartsFromRestoredPositionTwo() {
        
        ContentLister contentLister = new ContentLister() {
            
            private Map<Publisher,List<Content>> contents = ImmutableMap.<Publisher,List<Content>>of(
                    PA, ImmutableList.<Content>of(paItemOne),
                    BBC, ImmutableList.<Content>of(bbcItemOne),
                    C4, ImmutableList.<Content>of(c4ItemOne)
            );
            
            @Override
            public boolean listContent(Set<ContentTable> tables, ContentListingCriteria criteria, ContentListingHandler handler) {
                List<Content> pubContents = contents.get(Iterables.getOnlyElement(criteria.getPublishers()));
                handler.handle(pubContents, ContentListingProgress.progressFor(Iterables.getLast(pubContents), ContentTable.TOP_LEVEL_ITEMS));
                return true;
            }
        };
        
        context.checking(new Expectations(){{
            one(progressStore).progressForTask(with("pressassociation.com-bbc.co.uk-channel4.com-equivalence"));
                will(returnValue(new PublisherListingProgress(ContentListingProgress.START, PA)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, PA)));
            one(updater).updateEquivalences(paItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(paItemOne.getCanonicalUri(), PA)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, BBC)));
            one(updater).updateEquivalences(bbcItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(bbcItemOne.getCanonicalUri(), BBC)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, C4)));
            one(updater).updateEquivalences(c4ItemOne);
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(c4ItemOne.getCanonicalUri(), C4)));
            one(progressStore).storeProgress(with(any(String.class)), with(progressWith(null, null)));
        }});
        
        new ContentEquivalenceUpdateTask(contentLister, updater, log, progressStore ).forPublishers(PA, BBC, C4).run();
        
        context.assertIsSatisfied();
        
    }

    private Matcher<PublisherListingProgress> progressWith(final String uri, final Publisher publisher) {
        return new TypeSafeMatcher<PublisherListingProgress>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText(String.format("progress listing with uri %s and publisher %s", uri, publisher));
            }

            @Override
            public boolean matchesSafely(PublisherListingProgress input) {
                return Objects.equal(uri, input.getUri()) && Objects.equal(input.getPublisher(), publisher);
            }
        };
    }
    
}
