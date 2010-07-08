package org.atlasapi.equiv;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Equiv;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.remotesite.EquivGenerator;

public class BrandEquivUpdater implements EquivUpdater<Brand> {

    private final List<EquivGenerator<Brand>> equivGenerators;
    private final EquivalentUrlStore equivStore;

    public BrandEquivUpdater(List<EquivGenerator<Brand>> equivGenerators, EquivalentUrlStore equivStore) {
        this.equivGenerators = equivGenerators;
        this.equivStore = equivStore;
    }

    @Override
    public void update(Brand content) {
        for (EquivGenerator<Brand> generator: equivGenerators) {
            for (Equiv equiv: generator.equivalent(content)) {
                equivStore.store(equiv);
            }
        }
    }
}
