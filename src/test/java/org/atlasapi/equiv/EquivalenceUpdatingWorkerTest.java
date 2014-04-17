package org.atlasapi.equiv;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.equiv.results.persistence.CombinedEquivalenceScore;
import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.StoredEquivalenceResult;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.messaging.v3.EntityUpdatedMessage;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.Timestamp;

@RunWith(MockitoJUnitRunner.class)
public class EquivalenceUpdatingWorkerTest {

    private final ContentResolver resolver = mock(ContentResolver.class);
    private final LookupEntryStore entryStore = mock(LookupEntryStore.class);
    private final EquivalenceResultStore resultStore
        = mock(EquivalenceResultStore.class); 
    @SuppressWarnings("unchecked")
    private final EquivalenceUpdater<Content> updater = mock(EquivalenceUpdater.class);
    private final Predicate<Object> filter = Predicates.instanceOf(Item.class);
    @SuppressWarnings("unchecked")
    private final EquivalenceUpdatingWorker workerThatOnlyUpdatesItems
        = new EquivalenceUpdatingWorker(resolver, 
                entryStore, resultStore, updater, Predicates.<Content>and(filter));
    
    @Test
    public void testWorkerThatOnlyUpdatesItemsUpdatesAnItem() {
        
        String uri = "uri";
        Item item = new Item(uri, uri, BBC);
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, item).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", Timestamp.of(1L), "uri", "item", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater).updateEquivalences(item);
    }

    @Test
    public void testWorkerThatOnlyUpdatesItemsDoesntUpdateAnContainer() {
        
        String uri = "uri";
        Brand brand = new Brand(uri, uri, BBC);
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, brand).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", Timestamp.of(1L), "uri", "brand", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater, never()).updateEquivalences(brand);
    }
    
    @Test
    public void testWorkerThatOnlyUpdatesItemsHandlesResolvingNothing() {
        
        String uri = "uri";
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, null).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", Timestamp.of(1L), "uri", "brand", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater, never()).updateEquivalences(null);
    }

    @Test
    public void testWorkerThatOnlyUpdatesItemsHandlesResolvingNotContent() {
        
        String uri = "uri";
        Topic topic = new Topic(1L);
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, topic).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", Timestamp.of(1L), "uri", "brand", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater, never()).updateEquivalences(any(Content.class));
    }
    
    @Test
    public void testWorkerThatOnlyUpdatesItemsHandlesResolvingById() {
        
        String uri = "uri";
        Item item = new Item(uri, uri, BBC);
        item.setId(2L);
        when(entryStore.entriesForIds(ImmutableSet.of(item.getId())))
            .thenReturn(ImmutableList.of(LookupEntry.lookupEntryFrom(item)));
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, item).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", Timestamp.of(1L), "2", "item", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater).updateEquivalences(item);
        
    }
    
    @Test
    public void testWorkerDoesntUpdateItemWithAnEquivalenceResult() {
        
        String uri = "uri";
        Item item = new Item(uri, uri, BBC);
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, item).build());
        when(resultStore.forId(uri))
            .thenReturn(new StoredEquivalenceResult(uri, "title", 
                    HashBasedTable.<String, String, Double>create(), 
                    Lists.<CombinedEquivalenceScore>newArrayList(), 
                    new DateTime(DateTimeZones.UTC), Lists.newArrayList()));
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", Timestamp.of(1L), "uri", "item", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater, never()).updateEquivalences(any(Content.class));
        
    }
    
}
