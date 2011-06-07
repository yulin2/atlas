package org.atlasapi.query.content.schedule;

import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

public class BroadcastRemovingScheduleOverlapListener implements ScheduleOverlapListener {
    
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public BroadcastRemovingScheduleOverlapListener(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    @Override
    public void itemRemovedFromSchedule(Item item, Broadcast broadcast) {
        ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(ImmutableList.of(item.getCanonicalUri()));
        Maybe<Identified> identified = resolvedContent.get(item.getCanonicalUri());
        if (identified != null && identified.hasValue() && identified.requireValue() instanceof Item) {

            Item fullItem = (Item) identified.requireValue();
            for (Version version : fullItem.getVersions()) {
                Set<Broadcast> broadcasts = Sets.newHashSet();

                for (Broadcast b : version.getBroadcasts()) {
                    if (!broadcast.equals(b)) {
                        broadcasts.add(b);
                    }
                }

                version.setBroadcasts(broadcasts);
            }

            contentWriter.createOrUpdate(fullItem);
        }
    }
}
