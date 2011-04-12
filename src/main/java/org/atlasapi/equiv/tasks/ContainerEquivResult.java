package org.atlasapi.equiv.tasks;

import java.util.List;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.ImmutableList;

public class ContainerEquivResult<T, U> extends EquivResult<T>{

    private List<EquivResult<U>> itemResults = ImmutableList.of();

    public static ContainerEquivResult<Container<?>, Item> of(Container<?> brand, SuggestedEquivalents<Container<?>> countedEquivBrands, double certainty) {
        return new ContainerEquivResult<Container<?>, Item>(brand, brand.getContents().size(), countedEquivBrands, certainty);
    }
    
    public ContainerEquivResult(T desc, int fullMatch, SuggestedEquivalents<T> suggestedEquivs, double certainty) {
        super(desc, fullMatch, suggestedEquivs, certainty);
    }
    
    public ContainerEquivResult(EquivResult<T> base, Iterable<EquivResult<U>> subs) {
        super(base.described(), base.fullMatch(), base.suggestedEquivalents(), base.certainty());
        itemResults = ImmutableList.copyOf(subs);
    }
    
    public ContainerEquivResult<T, U> withItemResults(Iterable<EquivResult<U>> itemResults) {
        this.itemResults = ImmutableList.copyOf(itemResults);
        return this;
    }

    public List<EquivResult<U>> getItemResults() {
        return itemResults;
    }

}
