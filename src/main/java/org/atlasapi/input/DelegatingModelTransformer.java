package org.atlasapi.input;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;

public class DelegatingModelTransformer implements ModelTransformer<Description, Content> {

    private ModelTransformer<Item, org.atlasapi.media.entity.Item> itemTransformer;
    private ModelTransformer<org.atlasapi.media.entity.simple.Playlist, Brand> brandTransformer;

    public DelegatingModelTransformer(
            ModelTransformer<org.atlasapi.media.entity.simple.Playlist, Brand> brandTransformer,
            ModelTransformer<Item, org.atlasapi.media.entity.Item> itemTransformer) {
        this.brandTransformer = brandTransformer;
        this.itemTransformer = itemTransformer;
    }

    @Override
    public Content transform(Description simple) {
        if (simple instanceof Item) {
            return itemTransformer.transform((Item) simple);
        } else if (simple instanceof Playlist && simple.getType().equalsIgnoreCase("brand")) {
            Brand brand = brandTransformer.transform((Playlist) simple);
            return brand;
        }
        throw new IllegalArgumentException("Can't transform " + simple.getClass());
    }
}
