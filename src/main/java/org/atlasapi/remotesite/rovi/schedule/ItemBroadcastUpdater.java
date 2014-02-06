package org.atlasapi.remotesite.rovi.schedule;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;


public class ItemBroadcastUpdater {

    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    
    public ItemBroadcastUpdater(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = checkNotNull(contentResolver);
        this.contentWriter = checkNotNull(contentWriter);
    }
    
    public void addBroadcasts(String uri, Iterable<Broadcast> broadcasts) {
        Maybe<Identified> resolved = contentResolver.findByCanonicalUris(ImmutableSet.of(uri)).getFirstValue();
        
        if (resolved.isNothing()) {
            throw new IllegalStateException("Couldn't resolve " + uri);
        }
        
        Identified identified = resolved.requireValue();
        if (!(resolved.requireValue() instanceof Item)) {
            throw new IllegalArgumentException("Expecting the URI for an item but " + uri + " is a " 
                                                    + identified.getClass().getSimpleName());
        }
        Item item = (Item) identified;
        Iterables.getOnlyElement(item.getVersions()).getBroadcasts().addAll(ImmutableSet.copyOf(broadcasts));
        contentWriter.createOrUpdate(item);
    }
}
