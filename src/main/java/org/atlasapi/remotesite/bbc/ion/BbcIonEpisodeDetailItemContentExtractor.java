package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.content.Publisher.BBC;

import java.util.Set;

import org.atlasapi.media.content.Clip;
import org.atlasapi.media.content.Encoding;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
import org.atlasapi.remotesite.bbc.BbcProgrammeGraphExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonVersion;
import org.joda.time.Duration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonEpisodeDetailItemContentExtractor extends BaseBbcIonEpisodeItemExtractor implements ContentExtractor<IonEpisodeDetail, Item> {

    private final BbcProgrammeEncodingAndLocationCreator encodingCreator = new BbcProgrammeEncodingAndLocationCreator(new SystemClock());
    private final ContentExtractor<IonEpisodeDetail, Iterable<Clip>> clipExtractor; 
    
    public BbcIonEpisodeDetailItemContentExtractor(AdapterLog log, RemoteSiteClient<IonContainerFeed> containerClient) {
        super(log, containerClient);
        this.clipExtractor = new BbcIonClipExtractor(log);
    }

    @Override
    public Item extract(IonEpisodeDetail source) {
        Item baseItem = super.extract(source);
        baseItem.setVersions(extractVersions(source));
        baseItem.setClips(clipExtractor.extract(source));
        return baseItem;
    }
    
    private Set<Version> extractVersions(final IonEpisodeDetail source) {
        return Sets.newHashSet(Iterables.transform(source.getVersions(), new Function<IonVersion, Version>() {
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
