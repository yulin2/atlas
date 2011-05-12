package org.atlasapi.equiv.generators;

import java.util.List;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.results.ScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

public class ItemBasedContainerEquivalenceGenerator implements ContentEquivalenceGenerator<Container<?>> {

    private final ContentEquivalenceUpdater<Item> itemUpdater;

    public ItemBasedContainerEquivalenceGenerator(ContentEquivalenceUpdater<Item> itemUpdater) {
        this.itemUpdater = itemUpdater;
    }
    
    @Override
    public ScoredEquivalents<Container<?>> generateEquivalences(Container<?> container) {
        
        ScoredEquivalentsBuilder<Container<?>> containerEquivalents = ScoredEquivalents.fromSource("item");
        
        for (Item item : container.getContents()) {
            
            EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences(item);
            
            for (Entry<Publisher, List<ScoredEquivalent<Item>>> strongEquivalentBin : itemEquivalences.combinedEquivalences().entrySet()) {
                for (ScoredEquivalent<Item> strongEquivalent : strongEquivalentBin.getValue()) {
                    
                    Container<?> containerEquivalent = strongEquivalent.equivalent().getFullContainer();
                    if(containerEquivalent != null) {
                        containerEquivalents.addEquivalent(containerEquivalent, normalize(strongEquivalent.score(), container.getContents().size()));
                    }
                    
                }
            }
            
        }
        
        return containerEquivalents.build();
    }

    private double normalize(double score, int itemCount) {
        return score / itemCount;
    }

}
