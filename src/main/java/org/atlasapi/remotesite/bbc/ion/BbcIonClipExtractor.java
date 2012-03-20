package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.content.Publisher.BBC;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.content.Clip;
import org.atlasapi.media.content.Encoding;
import org.atlasapi.media.content.Location;
import org.atlasapi.media.content.Policy;
import org.atlasapi.media.content.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.intl.Countries;

public class BbcIonClipExtractor extends BaseBbcIonEpisodeItemExtractor implements
        ContentExtractor<IonEpisodeDetail, Iterable<Clip>> {

    public BbcIonClipExtractor(AdapterLog log) {
        super(log);
    }

    @Override
    public Iterable<Clip> extract(IonEpisodeDetail source) {
        return extractClips(source.getClips());
    }

    private Iterable<Clip> extractClips(List<IonEpisode> clips) {
        Set<Clip> extractedClips = Sets.newHashSet();
        for (IonEpisode ionClip : clips) {
            extractedClips.add(extractClip(ionClip));
        }
        return extractedClips;
    }

    private Clip extractClip(IonEpisode ionClip) {
        Clip clip = new Clip(BbcFeeds.slashProgrammesUriForPid(ionClip.getId()), CURIE_BASE
                + ionClip.getId(), BBC);
        clip.setIsLongForm(false);
        setItemDetails(clip, ionClip);
        setMediaTypeAndSpecialisation(clip, ionClip);
        clip.addVersion(clipVersion(ionClip));
        return clip;
    }

    private Version clipVersion(IonEpisode ionClip) {
        Version version = new Version();
        version.setCanonicalUri(BbcFeeds.slashProgrammesUriForPid(ionClip.getPlayVersionId()));
        version.setProvider(BBC);

        Encoding encoding = new Encoding();
        Location location = new Location();
        location.setTransportType(TransportType.LINK);
        location.setUri("http://www.bbc.co.uk/iplayer/episode/" + ionClip.getId());

        Policy policy = new Policy();
        policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
        policy.setAvailabilityStart(ionClip.getActualStart());
        policy.setAvailabilityEnd(ionClip.getAvailableUntil());

        location.setPolicy(policy);
        encoding.addAvailableAt(location);
        version.addManifestedAs(encoding);
        return version;
    }
}
