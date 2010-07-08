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

    @SuppressWarnings("static-access")
    @Override
    public void brandChanged(Collection<Brand> brands, changeType changeType) {
        if (changeType == changeType.CONTENT_UPDATE) {
            for (Brand brand: brands) {
                brandUpdater.update(brand);
            }
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public void itemChanged(Collection<Item> items, changeType changeType) {
        if (changeType == changeType.CONTENT_UPDATE) {
            for (Item item: items) {
                itemEquivUpdater.update(item);
            }
        }
    }
}
