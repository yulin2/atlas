package org.atlasapi.equiv;

import java.util.List;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Equiv;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.EquivGenerator;

import com.google.common.collect.ImmutableList;

public class ItemDelegatingToBrandEquivGenerator implements EquivGenerator<Item> {
    
    private final EquivGenerator<Container<?>> brandEquivGenerator;

    public ItemDelegatingToBrandEquivGenerator(EquivGenerator<Container<?>> brandEquivGenerator) {
        this.brandEquivGenerator = brandEquivGenerator;
    }

    @Override
    public List<Equiv> equivalent(Item content) {
        if (content != null && content.getContainer() != null) {
            return brandEquivGenerator.equivalent(content.getContainer());
        }
        return ImmutableList.of();
    }
}
