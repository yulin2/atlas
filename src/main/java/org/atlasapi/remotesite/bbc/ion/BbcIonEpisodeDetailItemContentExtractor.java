package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Restriction;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
import org.atlasapi.remotesite.bbc.BbcProgrammeGraphExtractor;
import org.atlasapi.remotesite.bbc.ion.IonService.MediaSetsToPoliciesFunction;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonVersion;
import org.atlasapi.remotesite.bbc.ion.model.IonVersionListFeed;
import org.joda.time.Duration;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonEpisodeDetailItemContentExtractor extends BaseBbcIonEpisodeItemExtractor implements ContentExtractor<IonEpisodeDetail, Item> {
    
    private final String VERSION_LIST_FORMAT = "http://www.bbc.co.uk/iplayer/ion/version/list/episode_id/%s/include_broadcasts/1/format/json";

    private final BbcProgrammeEncodingAndLocationCreator encodingCreator;
    private final ContentExtractor<IonEpisode, Clip> clipExtractor;
    private final BbcIonBroadcastExtractor broadcastExtractor;
    private RemoteSiteClient<IonVersionListFeed> versionListClient; 
    
    public BbcIonEpisodeDetailItemContentExtractor(AdapterLog log, RemoteSiteClient<IonContainerFeed> containerClient, MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction) {
        this(log, containerClient, null, mediaSetsToPoliciesFunction);
    }
    
    public BbcIonEpisodeDetailItemContentExtractor(AdapterLog log, RemoteSiteClient<IonContainerFeed> containerClient, RemoteSiteClient<IonVersionListFeed> versionListClient, MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction) {
        super(log, containerClient);
        this.versionListClient = versionListClient;
        this.clipExtractor = new BbcIonClipExtractor(log);
        this.broadcastExtractor = new BbcIonBroadcastExtractor();
        this.encodingCreator = new BbcProgrammeEncodingAndLocationCreator(mediaSetsToPoliciesFunction, new SystemClock());
    }

    @Override
    public Item extract(IonEpisodeDetail source) {
        Item baseItem = super.extract(source);
        baseItem.setVersions(extractVersions(source, getVersions(source)));
        baseItem.setClips(extractClips(source.getClips()));
        return baseItem;
    }
    
    private List<IonVersion> getVersions(IonEpisodeDetail source) {
        return versionListClient == null ? source.getVersions()
                                         : fetchVersions(source);
    }

    private List<IonVersion> fetchVersions(IonEpisodeDetail source) {
        try {
            return versionListClient.get(String.format(VERSION_LIST_FORMAT, source.getId())).getBlocklist();
        } catch (Exception e) {
            return source.getVersions();
        }
    }

    private Iterable<Clip> extractClips(List<IonEpisode> clips) {
        Set<Clip> extractedClips = Sets.newHashSet();
        for (IonEpisode ionClip : clips) {
            extractedClips.add(clipExtractor.extract(ionClip));
        }
        return extractedClips;
    }
    
    private Set<Version> extractVersions(final IonEpisodeDetail source, List<IonVersion> versions) {
        return Sets.newHashSet(Iterables.transform(versions, new Function<IonVersion, Version>() {
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
        version.setRestriction(restricitonFrom(ionVersion));
        if (ionVersion.getDuration() != null) {
            version.setDuration(Duration.standardSeconds(ionVersion.getDuration()));
        }
        if (ionVersion.getBroadcasts() != null) {
            for (IonBroadcast ionBroadcast : ionVersion.getBroadcasts()) {
                Maybe<Broadcast> broadcast = broadcastExtractor.extract(ionBroadcast);
                if (broadcast.hasValue()) {
                    version.addBroadcast(broadcast.requireValue());
                }
            }
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

    private Restriction restricitonFrom(IonVersion ionVersion) {
        return Strings.isNullOrEmpty(ionVersion.getGuidanceText())
                    ? Restriction.from()
                    : Restriction.from(ionVersion.getGuidanceText());
    }
}
