package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.media.entity.Publisher.PA;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class ContentEquivalenceUpdateTaskTest extends TestCase {

    private final AdapterLog log = new NullAdapterLog();
    private final Mockery context = new Mockery();
    
    @SuppressWarnings("unchecked") 
    private final ContentEquivalenceUpdater<Content> updater = context.mock(ContentEquivalenceUpdater.class);
    private final ScheduleTaskProgressStore progressStore = context.mock(ScheduleTaskProgressStore.class);

    private final Item paItemOne = new Item("pa1", "pa1c", Publisher.PA);
    private final Item bbcItemOne = new Item("bbc1", "bbc1c", Publisher.BBC);
    private final Item bbcItemTwo = new Item("bbc2", "bbc2c", Publisher.BBC);
    private final Item bbcItemThree = new Item("bbc3", "bbc3c", Publisher.BBC);
    private final Item c4ItemOne = new Item("c41", "c41c", Publisher.C4);

    private final ContentLister listerForContent(final Map<Publisher, List<Content>> contents) {
        return new ContentLister() {
            @Override
            public Iterator<Content> listContent(ContentListingCriteria criteria) {
                return Iterators.concat(Iterables.transform(criteria.getPublishers(), new Function<Publisher, Iterator<Content>>() {
                    @Override
                    public Iterator<Content> apply(Publisher input) {
                        return contents.get(input).iterator();
                    }
                }).iterator());
            }
        };
    }

    @Test
    public void testCallUpdateOnContent() {

        ContentLister contentLister = listerForContent(ImmutableMap.<Publisher,List<Content>>of(
            PA, ImmutableList.<Content>of(paItemOne),
            BBC, ImmutableList.<Content>of(bbcItemOne, bbcItemTwo, bbcItemThree),
            C4, ImmutableList.<Content>of(c4ItemOne)
        ));

        context.checking(new Expectations(){{
            one(progressStore).progressForTask(with("pressassociation.com-bbc.co.uk-channel4.com-equivalence"));
                will(returnValue(ContentListingProgress.START));
            one(updater).updateEquivalences(paItemOne, Optional.<List<Content>>absent());
            one(updater).updateEquivalences(bbcItemOne, Optional.<List<Content>>absent());
            one(updater).updateEquivalences(bbcItemTwo, Optional.<List<Content>>absent());
            one(updater).updateEquivalences(bbcItemThree, Optional.<List<Content>>absent());
            one(updater).updateEquivalences(c4ItemOne, Optional.<List<Content>>absent());
            one(progressStore).storeProgress(with(any(String.class)), with(ContentListingProgress.START));
        }});
        
        new ContentEquivalenceUpdateTask(contentLister, updater, log, progressStore, ImmutableSet.<String>of()).forPublishers(PA, BBC, C4).run();
        
        context.assertIsSatisfied();
        
    }
}
