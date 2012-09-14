package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Optional;

public class RootEquivalenceUpdater implements EquivalenceUpdater<Content> {

    private final EquivalenceUpdater<Container> containerUpdater;
    private final EquivalenceUpdater<Item> itemUpdater;

    public RootEquivalenceUpdater(EquivalenceUpdater<Container> containerUpdater, EquivalenceUpdater<Item> itemUpdater) {
        this.containerUpdater = containerUpdater;
        this.itemUpdater = itemUpdater;
    }

    @Override
    public EquivalenceResult<Content> updateEquivalences(Content content, Optional<List<Content>> externalCandidates) {
        if(content instanceof Container) {
            return updateContainer((Container)content);
        }
        if(content instanceof Item) {
            return updateItem((Item)content);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <Y extends Content> EquivalenceResult<Y> updateContainer(Container content) {
        EquivalenceResult<Y> updateEquivalences = (EquivalenceResult<Y>) containerUpdater.updateEquivalences(content, Optional.<List<Container>>absent());
        return updateEquivalences;
    }
    
    @SuppressWarnings("unchecked")
    private <Y extends Content> EquivalenceResult<Y> updateItem(Item content) {
        EquivalenceResult<Y> updateEquivalences = (EquivalenceResult<Y>) itemUpdater.updateEquivalences(content, Optional.<List<Item>>absent());
        return updateEquivalences;
    }

}
