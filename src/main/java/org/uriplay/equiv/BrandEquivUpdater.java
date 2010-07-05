package org.uriplay.equiv;

import java.util.List;

import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Equiv;
import org.uriplay.persistence.equiv.EquivStore;
import org.uriplay.remotesite.EquivGenerator;

public class BrandEquivUpdater implements EquivUpdater<Brand> {

    private final List<EquivGenerator<Brand>> equivGenerators;
    private final EquivStore equivStore;

    public BrandEquivUpdater(List<EquivGenerator<Brand>> equivGenerators, EquivStore equivStore) {
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
