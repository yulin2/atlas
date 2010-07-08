package org.atlasapi.equiv;

import java.util.List;

import org.atlasapi.media.entity.Equiv;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.remotesite.EquivGenerator;

public class ItemEquivUpdater implements EquivUpdater<Item> {
    
    private final List<EquivGenerator<Item>> equivGenerators;
    private final EquivalentUrlStore equivStore;

    public ItemEquivUpdater(List<EquivGenerator<Item>> equivGenerators, EquivalentUrlStore equivStore) {
        this.equivGenerators = equivGenerators;
        this.equivStore = equivStore;
    }

    @Override
    public void update(Item content) {
        for (EquivGenerator<Item> generator: equivGenerators) {
            for (Equiv equiv: generator.equivalent(content)) {
                equivStore.store(equiv);
            }
        }
    }
}
