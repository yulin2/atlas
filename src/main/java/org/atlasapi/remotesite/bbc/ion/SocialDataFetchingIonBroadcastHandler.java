package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.media.content.Identified;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.KeyPhrase;
import org.atlasapi.media.content.RelatedLink;
import org.atlasapi.media.topic.TopicRef;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class SocialDataFetchingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private final BbcExtendedDataContentAdapter extendedDataAdapter;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final AdapterLog log;

    public SocialDataFetchingIonBroadcastHandler(BbcExtendedDataContentAdapter extendedDataAdapter, ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        this.extendedDataAdapter = extendedDataAdapter;
        this.resolver = resolver;
        this.writer = writer;
        this.log = log;
    }

    @Override
    public void handle(IonBroadcast broadcast) {

        updateSocialDataFor(broadcast.getEpisodeId());

        if (broadcast.hasSeries()) {
            updateSocialDataFor(broadcast.getSeriesId());
        }
        if (broadcast.hasBrand()) {
            updateSocialDataFor(broadcast.getBrandId());
        }
        
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
        Maybe<Identified> possibleContent = resolver.findByCanonicalUris(ImmutableList.of(pidUri)).get(pidUri);
        if (possibleContent.hasValue()) {
            Content content = (Content) possibleContent.requireValue();
            content.setRelatedLinks(links);
            content.setKeyPhrases(phrases);
            content.setTopicRefs(topics);
            if (content instanceof Container) {
                writer.createOrUpdate((Container) content);
            } else if (content instanceof Item) {
                writer.createOrUpdate((Item) content);
            }
        }
    }

}
