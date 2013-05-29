package org.atlasapi.remotesite.bbc.ion;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class ItemAndPossibleBroadcast {

    private final Item item;
    private final Optional<Broadcast> broadcast;

    public ItemAndPossibleBroadcast(Item item, Optional<Broadcast> broadcast) {
        this.item = checkNotNull(item);
        this.broadcast = checkNotNull(broadcast);
    }
    
    public Item getItem() {
        return this.item;
    }

    public Optional<Broadcast> getBroadcast() {
        return this.broadcast;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof ItemAndPossibleBroadcast) {
            ItemAndPossibleBroadcast other = (ItemAndPossibleBroadcast) that;
            return item.equals(other.item)
                && broadcast.equals(other.broadcast);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return item.hashCode() ^ broadcast.hashCode();
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(getClass())
                .add("item", item)
                .add("broadcast", broadcast)
                .toString();
    }
}
