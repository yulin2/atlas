package org.atlasapi.equiv.update;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Brand;

@Deprecated
public class BrandEquivalenceUpdater implements ContentEquivalenceUpdater<Brand> {

    private final ContentEquivalenceGenerator<Brand> brandCalculator;

    public BrandEquivalenceUpdater(ContentEquivalenceGenerator<Brand> brandCalculator) {
        this.brandCalculator = brandCalculator;
    }
    
    @Override
    public EquivalenceResult updateEquivalences(Brand content) {
        ScoredEquivalents<Brand> calculateEquivalences = brandCalculator.generateEquivalences(content);
        return null;
    }

}
