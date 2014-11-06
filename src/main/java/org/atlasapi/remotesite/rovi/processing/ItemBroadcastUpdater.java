package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metabroadcast.common.base.Maybe;


public class ItemBroadcastUpdater {

    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    
    public ItemBroadcastUpdater(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = checkNotNull(contentResolver);
        this.contentWriter = checkNotNull(contentWriter);
    }
    
    public void addBroadcasts(String uri, Iterable<Broadcast> newBroadcasts, boolean replaceBroadcasts) {
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
        Version version = Iterables.getOnlyElement(item.getVersions());

        version.setBroadcasts(mergeOrReplaceBroadcasts(version, newBroadcasts, replaceBroadcasts));

        if (replaceBroadcasts) {
            version.setBroadcasts(Sets.newHashSet(newBroadcasts));
        } else {
            SetView<Broadcast> newBroadcastsSetForContent = mergeWithExistentBroadcasts(newBroadcasts, version);
            version.setBroadcasts(newBroadcastsSetForContent);
        }
        
        contentWriter.createOrUpdate(item);
    }

    private Set<Broadcast> mergeOrReplaceBroadcasts(Version version,
            Iterable<Broadcast> newBroadcasts,
            boolean replaceBroadcasts) {
        if (replaceBroadcasts) {
            return Sets.newHashSet(newBroadcasts);
        } else {
            return mergeWithExistentBroadcasts(newBroadcasts, version);
        }
    }

    private SetView<Broadcast> mergeWithExistentBroadcasts(Iterable<Broadcast> newBroadcasts,
            Version version) {
        Set<Broadcast> existingBroadcasts = version.getBroadcasts();
        return Sets.union(Sets.newHashSet(newBroadcasts), existingBroadcasts);
    }
}
