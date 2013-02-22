package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class SocialDataFetchingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private final BbcExtendedDataContentAdapter extendedDataAdapter;
    private final ContentStore store;
    private final AdapterLog log;

    public SocialDataFetchingIonBroadcastHandler(BbcExtendedDataContentAdapter extendedDataAdapter, ContentStore store, AdapterLog log) {
        this.extendedDataAdapter = extendedDataAdapter;
        this.store = store;
        this.log = log;
    }

    @Override
    public Maybe<ItemAndBroadcast> handle(IonBroadcast broadcast) {

        updateSocialDataFor(broadcast.getEpisodeId());

        if (broadcast.hasSeries()) {
            updateSocialDataFor(broadcast.getSeriesId());
        }
        if (broadcast.hasBrand()) {
            updateSocialDataFor(broadcast.getBrandId());
        }
        return Maybe.nothing();
        
    }

    private void updateSocialDataFor(String pid) { 
        String pidUri = BbcFeeds.slashProgrammesUriForPid(pid);
        try {
            
            Content content = extendedDataAdapter.fetch(pidUri);
           
            if (!content.getRelatedLinks().isEmpty() || !content.getKeyPhrases().isEmpty() || !content.getTopicRefs().isEmpty()) {
                upadteContent(pidUri, content.getRelatedLinks(), content.getKeyPhrases(), content.getTopicRefs());
            }
        } catch (Exception e) {
            log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Exception fetching social data for " + pidUri));
            Throwables.propagate(e);
        }
    }

    private void upadteContent(String pidUri, Set<RelatedLink> links, Set<KeyPhrase> phrases, List<TopicRef> topics) {
        Optional<Content> possibleContent = store.resolveAliases(ImmutableList.of(pidUri), Publisher.BBC).get(pidUri);
        if (possibleContent.isPresent()) {
            Content content = (Content) possibleContent.get();
            content.setRelatedLinks(links);
            content.setKeyPhrases(phrases);
            content.setTopicRefs(topics);
            store.writeContent(content);
        }
    }

}
