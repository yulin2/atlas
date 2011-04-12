package org.atlasapi.equiv.tasks;

import java.util.Set;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

public interface ItemEquivGenerator {

    public SuggestedEquivalents<Item> equivalentsFor(Item item);
    
    public Set<Publisher> supportedPublishers();

}