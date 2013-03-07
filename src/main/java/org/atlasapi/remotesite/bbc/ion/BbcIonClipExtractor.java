package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.intl.Countries;

public class BbcIonClipExtractor extends BaseBbcIonEpisodeItemExtractor implements
        ContentExtractor<IonEpisode, Clip> {

    public BbcIonClipExtractor(ContentResolver resolver) {
        super(null, resolver);
    }

    @Override
    public Clip extract(IonEpisode source) {
        return extractClip(source);
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
