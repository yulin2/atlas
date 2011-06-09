package org.atlasapi.equiv.generators;

import java.util.Set;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;

public class ItemBasedContainerEquivalenceGenerator implements ContentEquivalenceGenerator<Container<?>> {

    private static final String NAME = "Item";
    private final ContentEquivalenceUpdater<Item> itemUpdater;

    public ItemBasedContainerEquivalenceGenerator(ContentEquivalenceUpdater<Item> itemUpdater) {
        this.itemUpdater = itemUpdater;
    }
    
    @Override
    public ScoredEquivalents<Container<?>> generateEquivalences(Container<?> container, Set<Container<?>> suggestions) {
        
        ScoredEquivalentsBuilder<Container<?>> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
         for (Item item : container.getContents()) {
            
            EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences(item);

            for (ScoredEquivalent<Item> strongEquivalent : itemEquivalences.strongEquivalences().values()) {
                Container<?> containerEquivalent = strongEquivalent.equivalent().getFullContainer();
                if (containerEquivalent != null) {
                    containerEquivalents.addEquivalent(containerEquivalent, normalize(strongEquivalent.score(), container.getContents().size()));
                }
            }
            
        }
        
        return containerEquivalents.build();
    }

    private double normalize(double score, int itemCount) {
        return score / itemCount;
    }

}
