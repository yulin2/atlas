package org.atlasapi.equiv.update.tasks;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import junit.framework.TestCase;

import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(MockitoJUnitRunner.class)
public class ContentEquivalenceUpdateTaskTest extends TestCase {

    @SuppressWarnings("unchecked") 
    private final EquivalenceUpdater<Content> updater = mock(EquivalenceUpdater.class);
    private final ScheduleTaskProgressStore progressStore = mock(ScheduleTaskProgressStore.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);

    private final ContentLister listerForContent(final Multimap<Publisher, Content> contents) {
        return new ContentLister() {
            @Override
            public Iterator<Content> listContent(ContentListingCriteria criteria) {
                return Iterators.concat(Iterators.transform(criteria.getPublishers().iterator(), 
                    new Function<Publisher, Iterator<Content>>() {
                        @Override
                        public Iterator<Content> apply(Publisher input) {
                            return contents.get(input).iterator();
                        }
                    }
                ));
            }
        };
    }

    @Test
    public void testCallUpdateOnContent() {
        
        Item paItemOne = new Item("pa1", "pa1c", Publisher.PA);
        Item bbcItemOne = new Item("bbc1", "bbc1c", Publisher.BBC);
        Item bbcItemTwo = new Item("bbc2", "bbc2c", Publisher.BBC);
        Item bbcItemThree = new Item("bbc3", "bbc3c", Publisher.BBC);
        Item c4ItemOne = new Item("c41", "c41c", Publisher.C4);
        
        Brand paBrand = new Brand("paBrand", "paBrand", Publisher.PA);
        Episode paEp = new Episode("episode", "episode", Publisher.PA);
        paEp.setId(1);
        paEp.setThisOrChildLastUpdated(new DateTime(DateTimeZones.UTC));
        paBrand.setChildRefs(ImmutableList.of(paEp.childRef()));

        ContentLister contentLister = listerForContent(ImmutableMultimap.<Publisher,Content>builder()
            .putAll(PA, paItemOne, paBrand)
            .putAll(BBC, bbcItemOne, bbcItemTwo, bbcItemThree)
            .putAll(C4, c4ItemOne)
        .build());
        
        String taskName = "pressassociation.com-bbc.co.uk-channel4.com-equivalence";
        when(progressStore.progressForTask(taskName))
            .thenReturn(ContentListingProgress.START);
        
        when(contentResolver.findByCanonicalUris(argThat(hasItems("episode"))))
            .thenReturn(ResolvedContent.builder().put(paEp.getId(), paEp).build());
        
        new ContentEquivalenceUpdateTask(contentLister, contentResolver, progressStore, updater, ImmutableSet.<String>of()).forPublishers(PA, BBC, C4).run();
        
        verify(updater).updateEquivalences(paItemOne);
        verify(updater, times(2)).updateEquivalences(paBrand);
        verify(updater).updateEquivalences(paEp);
        verify(updater).updateEquivalences(bbcItemOne);
        verify(updater).updateEquivalences(bbcItemTwo);
        verify(updater).updateEquivalences(bbcItemThree);
        verify(updater).updateEquivalences(c4ItemOne);
        verify(progressStore).storeProgress(taskName, ContentListingProgress.START);
    }
}
