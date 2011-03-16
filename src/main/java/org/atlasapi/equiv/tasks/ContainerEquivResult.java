package org.atlasapi.equiv.tasks;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.ImmutableList;

public class ContainerEquivResult<T, U> extends EquivResult<T>{

    private List<EquivResult<U>> itemResults;

    public static ContainerEquivResult<Brand, Item> of(Brand brand, SuggestedEquivalents<Brand> countedEquivBrands, double certainty) {
        return new ContainerEquivResult<Brand, Item>(brand, brand.getContents().size(), countedEquivBrands, certainty);
    }
    
    public ContainerEquivResult(T desc, int fullMatch, SuggestedEquivalents<T> suggestedEquivs, double certainty) {
        super(desc, fullMatch, suggestedEquivs, certainty);
    }
    
    public ContainerEquivResult<T, U> withItemResults(Iterable<EquivResult<U>> itemResults) {
        this.itemResults = ImmutableList.copyOf(itemResults);
        return this;
    }

    public List<EquivResult<U>> getItemResults() {
        return itemResults;
    }

}
