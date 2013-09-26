package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.Sets;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.atlas.glycerin.model.Programme;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.collect.OptionalMap;
import com.metabroadcast.common.time.Clock;

/**
 * Base extractor for extracting common properties of {@link Item}s from a
 * {@link NitroItemSource}.
 * 
 * @param <SOURCE> - the type of {@link Programme}.
 * @param <ITEM> - the {@link Item} type extracted.
 */
public abstract class BaseNitroItemExtractor<SOURCE, ITEM extends Item>
    extends NitroContentExtractor<NitroItemSource<SOURCE>, ITEM> {

    private final NitroBroadcastExtractor broadcastExtractor
        = new NitroBroadcastExtractor();
    private final NitroAvailabilityExtractor availabilityExtractor
        = new NitroAvailabilityExtractor();
    
    public BaseNitroItemExtractor(Clock clock) {
        super(clock);
    }

    @Override
    protected final void extractAdditionalFields(NitroItemSource<SOURCE> source, ITEM item, DateTime now) {
        ImmutableSetMultimap<String, Broadcast> broadcasts = extractBroadcasts(source.getBroadcasts(), now);
        OptionalMap<String, Encoding> encodings = extractEncodings(source.getAvailabilities(), now);
        
        ImmutableSet.Builder<Version> versions = ImmutableSet.builder();
        for (String versionPid : Sets.union(broadcasts.keySet(), encodings.keySet())) {
            Version version = new Version();
            version.setCanonicalUri(BbcFeeds.nitroUriForPid(versionPid));
            version.setBroadcasts(broadcasts.get(versionPid));
            Optional<Encoding> versionEncoding = encodings.get(versionPid);
            if (versionEncoding.isPresent()) {
                version.setManifestedAs(ImmutableSet.of(versionEncoding.get()));
            }
            versions.add(version);
        }
        item.setVersions(versions.build());
        extractAdditionalItemFields(source, item, now);
    }

    /**
     * Concrete implementations can override this method to perform additional
     * configuration of the extracted content from the source.
     * 
     * @param source - the source data.
     * @param item - the extracted item.
     * @param now - the current time.
     */
    protected void extractAdditionalItemFields(NitroItemSource<SOURCE> source, ITEM item, DateTime now) {
        
    }

    private OptionalMap<String,Encoding> extractEncodings(List<Availability> availabilities, DateTime now) {
        Map<String, Collection<Availability>> byVersion = indexByVersion(availabilities);
        ImmutableMap.Builder<String, Encoding> results = ImmutableMap.builder();
        for (Entry<String, Collection<Availability>> availability : byVersion.entrySet()) {
            Optional<Encoding> extracted = availabilityExtractor.extract(availability.getValue());
            if (extracted.isPresent()) {
                results.put(availability.getKey(), setLastUpdated(extracted.get(), now));
            }
        }
        return ImmutableOptionalMap.fromMap(results.build());
    }

    private Encoding setLastUpdated(Encoding encoding, DateTime now) {
        encoding.setLastUpdated(now);
        for (Location location : encoding.getAvailableAt()) {
            location.setLastUpdated(now);
        }
        return encoding;
    }

    private Map<String, Collection<Availability>> indexByVersion(List<Availability> availabilities) {
        ImmutableSetMultimap.Builder<String, Availability> availabilitiesByVersion
            = ImmutableSetMultimap.builder();
        for (Availability availability : availabilities) {
            String versionPid = checkNotNull(NitroUtil.versionPid(availability));
            availabilitiesByVersion.put(versionPid, availability);
        }
        return availabilitiesByVersion.build().asMap();
    }

    private ImmutableSetMultimap<String, Broadcast> extractBroadcasts(
            List<com.metabroadcast.atlas.glycerin.model.Broadcast> nitroBroadcasts, DateTime now) {
        Builder<String, Broadcast> broadcasts = ImmutableSetMultimap.builder();
        for (com.metabroadcast.atlas.glycerin.model.Broadcast broadcast : nitroBroadcasts) {
            Optional<Broadcast> extractedBroadcast = broadcastExtractor.extract(broadcast);
            if (extractedBroadcast.isPresent()) {
                broadcasts.put(versionPid(broadcast), setLastUpdated(extractedBroadcast.get(), now));
            }
        }
        return broadcasts.build();
    }

    private Broadcast setLastUpdated(Broadcast broadcast, DateTime now) {
        broadcast.setLastUpdated(now);
        return broadcast;
    }

    private String versionPid(com.metabroadcast.atlas.glycerin.model.Broadcast broadcast) {
        for (PidReference pidRef : broadcast.getBroadcastOf()) {
            if ("version".equals(pidRef.getResultType())) {
                return pidRef.getPid();
            }
        }
        throw new IllegalArgumentException(String.format("No version ref for %s %s",
                broadcast.getClass().getSimpleName(), broadcast.getPid()));
    }
    
}
