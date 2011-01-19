package org.atlasapi.equiv;

import org.atlasapi.media.entity.Container;
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
	public void brandChanged(Iterable<? extends Container<?>> containers, ChangeType changeType) {
        if (ChangeType.CONTENT_UPDATE.equals(changeType)) {
            for (Container<?> brand: containers) {
                brandUpdater.update(brand);
            }
        }
    }

    @Override
	public void itemChanged(Iterable<? extends Item> items, ChangeType changeType) {
        if (ChangeType.CONTENT_UPDATE.equals(changeType)) {
            for (Item item: items) {
                itemEquivUpdater.update(item);
            }
        }
    }
}
