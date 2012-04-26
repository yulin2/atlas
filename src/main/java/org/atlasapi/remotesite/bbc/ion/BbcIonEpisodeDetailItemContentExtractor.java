package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
import org.atlasapi.remotesite.bbc.BbcProgrammeGraphExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonVersion;
import org.joda.time.Duration;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonEpisodeDetailItemContentExtractor extends BaseBbcIonEpisodeItemExtractor implements ContentExtractor<IonEpisodeDetail, Item> {

    private final BbcProgrammeEncodingAndLocationCreator encodingCreator = new BbcProgrammeEncodingAndLocationCreator(new SystemClock());
    
    public BbcIonEpisodeDetailItemContentExtractor(AdapterLog log, RemoteSiteClient<IonContainerFeed> containerClient) {
        super(log, containerClient);
    }

    @Override
    public Item extract(IonEpisodeDetail source) {
        Item baseItem = super.extract(source);
        baseItem.setVersions(extractVersions(source));
        baseItem.setClips(extractClips(source.getClips()));
        return baseItem;
    }
    
    private Iterable<Clip> extractClips(List<IonEpisode> clips) {
        Builder<Clip> extractedClips = ImmutableList.builder();
        for (IonEpisode ionClip : clips) {
            extractedClips.add(extractClip(ionClip));
        }
        return extractedClips.build();
    }

    private Clip extractClip(IonEpisode ionClip) {
        Clip clip = new Clip(BbcFeeds.slashProgrammesUriForPid(ionClip.getId()), CURIE_BASE+ionClip.getId(), BBC);
        clip.setIsLongForm(false);
        setItemDetails(clip, ionClip);
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

    private Set<Version> extractVersions(final IonEpisodeDetail source) {
        return ImmutableSet.copyOf(Iterables.transform(source.getVersions(), new Function<IonVersion, Version>() {
            @Override
            public Version apply(IonVersion input) {
                return versionFrom(input, source.getId());
            }
        }));
    }

    private Version versionFrom(IonVersion ionVersion, String pid) {
        Version version = new Version();
        version.setCanonicalUri(BbcFeeds.slashProgrammesUriForPid(ionVersion.getId()));
        BbcProgrammeGraphExtractor.setDurations(version, ionVersion);
        version.setProvider(BBC);
        if (ionVersion.getDuration() != null) {
            version.setDuration(Duration.standardSeconds(ionVersion.getDuration()));
        }
        if (ionVersion.getOndemands() != null) {
            for (IonOndemandChange ondemand : ionVersion.getOndemands()) {
                Maybe<Encoding> possibleEncoding = encodingCreator.createEncoding(ondemand, pid);
                if (possibleEncoding.hasValue()) {
                    version.addManifestedAs(possibleEncoding.requireValue());
                }
            }
        }
        return version;
    }
}
