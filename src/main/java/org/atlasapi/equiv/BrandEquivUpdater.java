package org.atlasapi.equiv;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Equiv;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.remotesite.EquivGenerator;

public class BrandEquivUpdater implements EquivUpdater<Container<?>> {

    private final List<EquivGenerator<Container<?>>> equivGenerators;
    private final EquivalentUrlStore equivStore;
    private final Log log = LogFactory.getLog(BrandEquivUpdater.class);

    public BrandEquivUpdater(List<EquivGenerator<Container<?>>> equivGenerators, EquivalentUrlStore equivStore) {
        this.equivGenerators = equivGenerators;
        this.equivStore = equivStore;
    }

    @Override
    public void update(Container<?> content) {
        for (EquivGenerator<Container<?>> generator : equivGenerators) {
            try {
                for (Equiv equiv : generator.equivalent(content)) {
                    equivStore.store(equiv);
                }
            } catch (Exception e) {
                log.warn("Unable to get Equivalents for " + content, e);
            }
        }
    }
}
