package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetailFeed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class BbcIonEpisodeItemAdapter<C> implements SiteSpecificAdapter<C> {

    private static final String EPISODE_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/episodedetail/episode/%s/include_broadcasts/1/clips/include/next_broadcasts/1/allow_unavailable/1/category_type/pips/format/json";

    private final RemoteSiteClient<IonEpisodeDetailFeed> client;
    private final ContentExtractor<? super IonEpisodeDetail, C> extractor;

    public BbcIonEpisodeItemAdapter(RemoteSiteClient<IonEpisodeDetailFeed> client, ContentExtractor<? super IonEpisodeDetail, C> extractor) {
        this.client = client;
        this.extractor = extractor;
    }
    
    @Override
    public C fetch(String uri) {
        Preconditions.checkArgument(BbcFeeds.isACanonicalSlashProgrammesUri(uri), "Can't fetch episode detail uri %s", uri);
        try {
            IonEpisodeDetailFeed ionEpisodeDetailFeed = client.get(String.format(EPISODE_DETAIL_PATTERN, BbcFeeds.pidFrom(uri)));
            if (!ionEpisodeDetailFeed.getBlocklist().isEmpty()) {
                return extractor.extract(Iterables.getOnlyElement(ionEpisodeDetailFeed.getBlocklist()));
            }
            return null;
        } catch (Exception e) {
            throw new FetchException(uri, e);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }
}
