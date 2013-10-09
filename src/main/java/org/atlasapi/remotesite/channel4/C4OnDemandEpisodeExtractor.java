package org.atlasapi.remotesite.channel4;

import java.util.Map;

import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.media.entity.Version;
import org.jdom.Element;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

final class C4OnDemandEpisodeExtractor extends BaseC4EpisodeExtractor {

    private final C4AtomEntryVersionExtractor versionExtractor;

    public C4OnDemandEpisodeExtractor(Optional<Platform> platform, Clock clock) {
        super(clock);
        versionExtractor = new C4AtomEntryVersionExtractor(platform, clock);
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
        episode.addVersion(setLastUpdated(versionExtractor.extract(entry), episode.getLastUpdated()));
        return episode;
    }
    
    private Version setLastUpdated(Version version, DateTime lastUpdated) {
        version.setLastUpdated(lastUpdated);
        for (Encoding encoding : version.getManifestedAs()) {
            encoding.setLastUpdated(lastUpdated);
            for (Location location : encoding.getAvailableAt()) {
                location.setLastUpdated(lastUpdated);
            }
        }
        return version;
    }

    @Override
    protected Element getMedia(Entry source) {
        return C4AtomApi.mediaGroup(source);
    }
    
}
