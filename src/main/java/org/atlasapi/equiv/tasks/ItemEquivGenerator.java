package org.atlasapi.equiv.tasks;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

public interface ItemEquivGenerator {

    public SuggestedEquivalents<Item> equivalentsFor(Item item, Iterable<Publisher> targetPublishers);

}