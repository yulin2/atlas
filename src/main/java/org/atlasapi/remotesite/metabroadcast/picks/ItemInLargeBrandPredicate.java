package org.atlasapi.remotesite.metabroadcast.picks;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;


/**
 * A predicate that will return true iff the {@link Item} is part of a brand with
 * many children
 * 
 * @author tom
 *
 */
public class ItemInLargeBrandPredicate implements Predicate<ItemAndBroadcast> {

    private final ContentResolver contentResolver;
    private final int largeBrandSize;
    
    public ItemInLargeBrandPredicate(ContentResolver contentResolver, int largeBrandSize) {
        this.contentResolver = checkNotNull(contentResolver);
        this.largeBrandSize = largeBrandSize;
    }
    
    @Override
    public boolean apply(ItemAndBroadcast itemAndBroadcast) {
        Item item = itemAndBroadcast.getItem();
        if (item.getContainer() == null) {
            return false;
        }
        
        Container c = (Container) contentResolver.findByCanonicalUris(ImmutableSet.of(item.getContainer().getUri()))
                                                  .getFirstValue().requireValue();
        return c.getChildRefs().size() >= largeBrandSize;
    }

}
