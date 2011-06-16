package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;

public class RootEquivalenceUpdater implements ContentEquivalenceUpdater<Content> {

    private final ContentEquivalenceUpdater<Container<?>> containerUpdater;
    private final ContentEquivalenceUpdater<Item> itemUpdater;

    public RootEquivalenceUpdater(ContentEquivalenceUpdater<Container<?>> containerUpdater, ContentEquivalenceUpdater<Item> itemUpdater) {
        this.containerUpdater = containerUpdater;
        this.itemUpdater = itemUpdater;
    }

    @Override
    public EquivalenceResult<Content> updateEquivalences(Content content) {
        if(content instanceof Container<?>) {
            return updateContainer((Container<?>)content);
        }
        if(content instanceof Item) {
            return updateItem((Item)content);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <Y extends Content> EquivalenceResult<Y> updateContainer(Container<?> content) {
        EquivalenceResult<Y> updateEquivalences = (EquivalenceResult<Y>) containerUpdater.updateEquivalences(content);
        return updateEquivalences;
    }
    
    @SuppressWarnings("unchecked")
    private <Y extends Content> EquivalenceResult<Y> updateItem(Item content) {
        EquivalenceResult<Y> updateEquivalences = (EquivalenceResult<Y>) itemUpdater.updateEquivalences(content);
        return updateEquivalences;
    }

}
