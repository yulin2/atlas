package org.atlasapi.equiv;

import java.util.Collection;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentListener;

public class EquivContentListener implements ContentListener {

    private final BrandEquivUpdater brandUpdater;
    private final ItemEquivUpdater itemEquivUpdater;

    public EquivContentListener(BrandEquivUpdater brandUpdater, ItemEquivUpdater itemEquivUpdater) {
        this.brandUpdater = brandUpdater;
        this.itemEquivUpdater = itemEquivUpdater;
    }

    @Override
    public void brandChanged(Collection<Brand> brands, changeType type) {
        if (changeType.CONTENT_UPDATE.equals(type)) {
            for (Brand brand: brands) {
                brandUpdater.update(brand);
            }
        }
    }

    @Override
    public void itemChanged(Collection<Item> items, changeType type) {
        if (changeType.CONTENT_UPDATE.equals(type)) {
            for (Item item: items) {
                itemEquivUpdater.update(item);
            }
        }
    }
}
