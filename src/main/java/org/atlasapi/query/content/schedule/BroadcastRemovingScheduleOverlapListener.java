package org.atlasapi.query.content.schedule;

import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.Sets;

public class BroadcastRemovingScheduleOverlapListener implements ScheduleOverlapListener {
    
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public BroadcastRemovingScheduleOverlapListener(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }

    @Override
    public void itemRemovedFromSchedule(Item item, Broadcast broadcast) {
        Identified identified = contentResolver.findByCanonicalUri(item.getCanonicalUri());
        if (identified instanceof Item) {

            Item fullItem = (Item) identified;
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
