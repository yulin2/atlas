package org.atlasapi.remotesite.channel4;

import java.util.Map;

import org.atlasapi.media.entity.Episode;
import org.jdom.Element;

import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

final class C4EpisodeGuideEpisodeExtractor extends BaseC4EpisodeExtractor {

    public C4EpisodeGuideEpisodeExtractor(Clock clock) {
        super(clock);
    }

    @Override
    protected Episode setAdditionalEpisodeFields(Entry entry, Map<String, String> lookup,
            Episode episode) {
        episode.addAliasUrl(C4AtomApi.hierarchyEpisodeUri(entry));
        return episode;
    }

    @Override
    protected Element getMedia(Entry source) {
        return C4AtomApi.mediaContent(source);
    }
}
