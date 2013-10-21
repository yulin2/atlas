package org.atlasapi.remotesite.channel4.pmlsd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Policy.Platform;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

final class C4OnDemandEpisodeExtractor extends BaseC4EpisodeExtractor {

    private final C4AtomEntryVersionExtractor versionExtractor;

    public C4OnDemandEpisodeExtractor(Optional<Platform> platform, Clock clock) {
        super(clock);
        versionExtractor = new C4AtomEntryVersionExtractor(platform);
    }

    @Override
    protected Episode setAdditionalEpisodeFields(Entry entry, Map<String, String> lookup,
            Episode episode) {
        String fourOdUri = C4AtomApi.fourOdUri(entry);
        if (fourOdUri != null) {
            episode.addAliasUrl(fourOdUri);
        }
        String seriesEpisodeUri = C4AtomApi.canonicalUri(entry);
        if(seriesEpisodeUri != null) {
            episode.addAliasUrl(seriesEpisodeUri);
        }
        episode.addVersion(versionExtractor.extract(data(entry, fourOdUri, lookup, episode.getLastUpdated())));
        return episode;
    }
    
    private C4VersionData data(Entry entry, String fourOdUri, Map<String, String> lookup, DateTime lastUpdated) {
        String uri = fourOdUri != null ? fourOdUri : C4AtomApi.clipUri(entry);
        checkNotNull(uri, "No version URI extracted for %s", entry.getId());
        return new C4VersionData(entry.getId(), uri, getMedia(entry), lookup, lastUpdated);
    }

}
