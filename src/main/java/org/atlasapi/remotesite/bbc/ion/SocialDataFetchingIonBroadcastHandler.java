package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class SocialDataFetchingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private final SiteSpecificAdapter<List<RelatedLink>> relatedLinkAdapter;
    private final SiteSpecificAdapter<List<KeyPhrase>> hashTagAdapter;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final AdapterLog log;
    private SiteSpecificAdapter<List<TopicRef>> topicsAdapter;

    public SocialDataFetchingIonBroadcastHandler(SiteSpecificAdapter<List<RelatedLink>> linkAdapter, SiteSpecificAdapter<List<KeyPhrase>> phraseAdapter, SiteSpecificAdapter<List<TopicRef>> topicsAdapter, ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        this.resolver = resolver;
        this.writer = writer;
        this.log = log;
        this.relatedLinkAdapter = linkAdapter;
        this.hashTagAdapter = phraseAdapter;
        this.topicsAdapter = topicsAdapter;
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
            List<RelatedLink> links = relatedLinkAdapter.fetch(pidUri);
            List<KeyPhrase> phrases = hashTagAdapter.fetch(pidUri);
            List<TopicRef> topics = topicsAdapter.fetch(pidUri);
           
            if (!links.isEmpty() || !phrases.isEmpty() || !topics.isEmpty()) {
                upadteContent(pidUri, links, phrases, topics);
            }
        } catch (Exception e) {
            log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Exception fetching social data for " + pidUri));
        }
    }

    private void upadteContent(String pidUri, List<RelatedLink> links, List<KeyPhrase> phrases, List<TopicRef> topics) {
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
