package org.atlasapi.equiv;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.messaging.v3.EntityUpdatedMessage;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class EquivalenceUpdatingWorkerTest {

    private final ContentResolver resolver = mock(ContentResolver.class);
    @SuppressWarnings("unchecked")
    private final EquivalenceUpdater<Content> updater = mock(EquivalenceUpdater.class);
    private final Predicate<Object> filter = Predicates.instanceOf(Item.class);
    @SuppressWarnings("unchecked")
    private final EquivalenceUpdatingWorker workerThatOnlyUpdatesItems
        = new EquivalenceUpdatingWorker(resolver, updater, Predicates.<Content>and(filter));
    
    @Test
    public void testWorkerThatOnlyUpdatesItemsUpdatesAnItem() {
        
        String uri = "uri";
        Item item = new Item(uri, uri, BBC);
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
            .thenReturn(ResolvedContent.builder().put(uri, item).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", 1L, "uri", "item", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater).updateEquivalences(item);
    }

    @Test
    public void testWorkerThatOnlyUpdatesItemsDoesntUpdateAnContainer() {
        
        String uri = "uri";
        Brand brand = new Brand(uri, uri, BBC);
        when(resolver.findByCanonicalUris(ImmutableSet.of(uri)))
        .thenReturn(ResolvedContent.builder().put(uri, brand).build());
        
        EntityUpdatedMessage msg = new EntityUpdatedMessage("1", 1L, "uri", "brand", "bbc.co.uk");
        workerThatOnlyUpdatesItems.process(msg);
        
        verify(updater, never()).updateEquivalences(brand);
    }

}
