package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.bbc.BbcFeeds;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.atlas.glycerin.model.Programme;
import com.metabroadcast.common.collect.ImmutableOptionalMap;
import com.metabroadcast.common.collect.OptionalMap;

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
    
    @Override
    protected final void extractAdditionalFields(NitroItemSource<SOURCE> source, ITEM item) {
        ImmutableSetMultimap<String, Broadcast> broadcasts = extractBroadcasts(source.getBroadcasts());
        Map<String, Optional<Encoding>> encodings = extractEncodings(source.getAvailabilities());
        
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
        extractAdditionalItemFields(source, item);
    }

    /**
     * Concrete implementations can override this method to perform additional
     * configuration of the extracted content from the source.
     * 
     * @param source - the source data.
     * @param item - the extracted item.
     */
    protected void extractAdditionalItemFields(NitroItemSource<SOURCE> source, ITEM item) {
        
    }

    private OptionalMap<String,Encoding> extractEncodings(List<Availability> availabilities) {
        ImmutableSetMultimap.Builder<String, Availability> availabilitiesByVersion
            = ImmutableSetMultimap.builder();
        for (Availability availability : availabilities) {
            availabilitiesByVersion.put(checkNotNull(NitroUtil.versionPid(availability)), availability);
        }
        return ImmutableOptionalMap.copyOf(Maps.transformValues(availabilitiesByVersion.build().asMap(),
                new Function<Collection<Availability>, Optional<Encoding>>() {
                    @Override
                    public Optional<Encoding> apply(Collection<Availability> input) {
                        return availabilityExtractor.extract(input);
                    }
                }));
    }

    private ImmutableSetMultimap<String, Broadcast> extractBroadcasts(
            List<com.metabroadcast.atlas.glycerin.model.Broadcast> nitroBroadcasts) {
        Builder<String, Broadcast> broadcasts = ImmutableSetMultimap.builder();
        for (com.metabroadcast.atlas.glycerin.model.Broadcast broadcast : nitroBroadcasts) {
            Optional<Broadcast> extractedBroadcast = broadcastExtractor.extract(broadcast);
            if (extractedBroadcast.isPresent()) {
                broadcasts.put(versionPid(broadcast), extractedBroadcast.get());
            }
        }
        return broadcasts.build();
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
