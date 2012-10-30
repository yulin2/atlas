package org.atlasapi.input;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;

public class DelegatingModelTransformer implements ModelTransformer<Description, Content> {

    private ModelTransformer<Item, org.atlasapi.media.entity.Item> itemTransformer;

    public DelegatingModelTransformer(ModelTransformer<Item,org.atlasapi.media.entity.Item> itemTransformer) {
        this.itemTransformer = itemTransformer;
    }
    
    @Override
    public Content transform(Description simple) {
        if (simple instanceof Item) {
            return itemTransformer.transform((Item) simple);
        }
        throw new IllegalArgumentException("Can't transform " + simple.getClass());
    }


}
