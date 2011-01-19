package org.atlasapi.equiv;

import java.util.List;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Equiv;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.remotesite.EquivGenerator;

public class BrandEquivUpdater implements EquivUpdater<Container<?>> {

    private final List<EquivGenerator<Container<?>>> equivGenerators;
    private final EquivalentUrlStore equivStore;

    public BrandEquivUpdater(List<EquivGenerator<Container<?>>> equivGenerators, EquivalentUrlStore equivStore) {
        this.equivGenerators = equivGenerators;
        this.equivStore = equivStore;
    }

    @Override
    public void update(Container<?> content) {
        for (EquivGenerator<Container<?>> generator: equivGenerators) {
            for (Equiv equiv: generator.equivalent(content)) {
                equivStore.store(equiv);
            }
        }
    }
}
