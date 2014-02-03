package org.atlasapi.remotesite.bbc.ion;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;

import com.google.common.base.Preconditions;

public class BbcExtendedDataContentAdapter {

    private final SiteSpecificAdapter<List<RelatedLink>> linkAdapter;
    private final SiteSpecificAdapter<List<TopicRef>> topicsAdapter;

    public BbcExtendedDataContentAdapter(SiteSpecificAdapter<List<RelatedLink>> linkAdapter, SiteSpecificAdapter<List<TopicRef>> topicsAdapter) {
        this.linkAdapter = linkAdapter;
        this.topicsAdapter = topicsAdapter;
    }

    public Content fetch(String uri) {
        Preconditions.checkArgument(canFetch(uri), "Invalid URI %s", uri);
        List<RelatedLink> links = linkAdapter.fetch(uri);
        List<TopicRef> topics = topicsAdapter.fetch(uri);

        Content content = new Item();
        content.setRelatedLinks(links);
        content.setTopicRefs(topics);
        return content;

    }

    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }

}
