package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;

import java.util.List;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class SegmentUpdatingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private final ContentStore store;
    private final BbcIonSegmentAdapter segmentAdapter;

    public SegmentUpdatingIonBroadcastHandler(ContentStore store, BbcIonSegmentAdapter segmentAdapter) {
        this.store = store;
        this.segmentAdapter = segmentAdapter;
    }

    @Override
    public Maybe<ItemAndBroadcast> handle(IonBroadcast broadcast) {
        
        final String itemId = BbcFeeds.slashProgrammesUriForPid(broadcast.getEpisodeId());
        Alias itemProgrammesUrl = new Alias("bbc:programmes:url", itemId);
        Optional<Content> possibleContent = store.resolveAliases(ImmutableSet.of(itemProgrammesUrl), BBC).get(itemProgrammesUrl);
        
        if(possibleContent.isPresent()) {
            
            Item item = (Item) possibleContent.get();
            
            Version version = versionFrom(BbcFeeds.slashProgrammesUriForPid(broadcast.getVersionId()), item);
            if (version != null) {
                List<SegmentEvent> segEvents = segmentAdapter.fetch(broadcast.getVersionId());
                version.addSegmentEvents(segEvents);
                store.writeContent(item);
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
