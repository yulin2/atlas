package org.atlasapi.equiv.generators;

import java.util.Set;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class ItemBasedContainerEquivalenceGenerator implements ContentEquivalenceGenerator<Container<?>> {

    private static final String NAME = "Item";
    private final ContentEquivalenceUpdater<Item> itemUpdater;
    private final ContentResolver resolver;

    public ItemBasedContainerEquivalenceGenerator(ContentEquivalenceUpdater<Item> itemUpdater, ContentResolver resolver) {
        this.itemUpdater = itemUpdater;
        this.resolver = resolver;
    }
    
    @Override
    public ScoredEquivalents<Container<?>> generateEquivalences(Container<?> container, Set<Container<?>> suggestions) {
        
        ScoredEquivalentsBuilder<Container<?>> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
         for (ChildRef ref : container.getChildRefs()) {
            
            String itemUri = ref.getUri();
            Maybe<Identified> maybeItem = resolver.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
            
            if(maybeItem.hasValue()) {
                
                Item item = (Item) maybeItem.requireValue();
                
                EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences(item);
                
                for (ScoredEquivalent<Item> strongEquivalent : itemEquivalences.strongEquivalences().values()) {
                    ParentRef parentEquivalent = strongEquivalent.equivalent().getContainer();
                    if (resolve(parentEquivalent) != null) {
                        containerEquivalents.addEquivalent(resolve(parentEquivalent), normalize(strongEquivalent.score(), container.getChildRefs().size()));
                    }
                }
            }
            
        }
        
        return containerEquivalents.build();
    }

    private Container<?> resolve(ParentRef parentEquivalent) {
        return (Container<?>) resolver.findByCanonicalUris(ImmutableList.of(parentEquivalent.getUri())).getFirstValue().requireValue();
    }

    private double normalize(double score, int itemCount) {
        return score / itemCount;
    }

}
