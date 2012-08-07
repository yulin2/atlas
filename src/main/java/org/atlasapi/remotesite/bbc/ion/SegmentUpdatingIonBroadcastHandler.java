package org.atlasapi.remotesite.bbc.ion;

import java.util.List;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class SegmentUpdatingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    private final BbcIonSegmentAdapter segmentAdapter;

    public SegmentUpdatingIonBroadcastHandler(ContentResolver contentResolver, ContentWriter contentWriter, BbcIonSegmentAdapter segmentAdapter) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
        this.segmentAdapter = segmentAdapter;
    }

    @Override
    public Maybe<ItemAndBroadcast> handle(IonBroadcast broadcast) {
        
        final String itemId = BbcFeeds.slashProgrammesUriForPid(broadcast.getEpisodeId());
        Maybe<Identified> possibleContent = contentResolver.findByCanonicalUris(ImmutableSet.of(itemId)).get(itemId);
        
        if(possibleContent.hasValue()) {
            
            Item item = (Item) possibleContent.requireValue();
            
            Version version = versionFrom(BbcFeeds.slashProgrammesUriForPid(broadcast.getVersionId()), item);
            if (version != null) {
                List<SegmentEvent> segEvents = segmentAdapter.fetch(broadcast.getVersionId());
                version.addSegmentEvents(segEvents);
                contentWriter.createOrUpdate(item);
            }
            
            return Maybe.just(new ItemAndBroadcast(item, Maybe.<Broadcast>nothing()));
        }
        return Maybe.nothing();
    }

    private Version versionFrom(String versionUri, Item item) {
        for (Version version : item.getVersions()) {
            if(versionUri.equals(version.getCanonicalUri())) {
                return version;
            }
        }
        return null;
    }

}
