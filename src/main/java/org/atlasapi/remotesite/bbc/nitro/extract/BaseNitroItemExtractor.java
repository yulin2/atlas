package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Restriction;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.Iterables;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.atlas.glycerin.model.Programme;
import com.metabroadcast.atlas.glycerin.model.VersionType;
import com.metabroadcast.atlas.glycerin.model.VersionTypes;
import com.metabroadcast.atlas.glycerin.model.WarningText;
import com.metabroadcast.atlas.glycerin.model.Warnings;
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

    private static final String AUDIO_DESCRIBED_VERSION_TYPE = "DubbedAudioDescribed";

    private final String WIDESCREEN_RATIO = "16:9";

    private final Predicate<WarningText> IS_SHORT_WARNING = new Predicate<WarningText>() {
        @Override
        public boolean apply(WarningText input) {
            return "short".equals(input.getLength());
        }
    };

    private static final Predicate<VersionType> IS_AUDIO_DESCRIBED = new Predicate<VersionType>() {
        @Override
        public boolean apply(VersionType input) {
            return AUDIO_DESCRIBED_VERSION_TYPE.equals(input.getId());
        }
    };

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
        for (com.metabroadcast.atlas.glycerin.model.Version nitroVersion : source.getVersions()) {
            Version version = new Version();
            version.setDuration(convertDuration(nitroVersion.getDuration()));
            version.setLastUpdated(now);
            version.setCanonicalUri(BbcFeeds.nitroUriForPid(nitroVersion.getPid()));
            version.setBroadcasts(completeBroadcastsWithVersion(broadcasts, nitroVersion));
            Optional<Encoding> optVersionEncoding = encodings.get(nitroVersion.getPid());

            if (optVersionEncoding.isPresent()) {
                Encoding versionEncoding = optVersionEncoding.get();
                versionEncoding.setVideoAspectRatio(nitroVersion.getAspectRatio());
                versionEncoding.setAudioDescribed(isAudioDescribed(nitroVersion));
                version.setManifestedAs(ImmutableSet.of(versionEncoding));
            }

            Optional<WarningText> warningText = warningTextFrom(nitroVersion);
            if (warningText.isPresent()) {
                Restriction restriction = new Restriction();
                restriction.setRestricted(true);
                restriction.setMessage(warningText.get().getValue());
            }

            versions.add(version);
        }
        item.setVersions(versions.build());
        if (item instanceof Film) {
            item.setMediaType(MediaType.VIDEO);
            item.setSpecialization(Specialization.FILM);
        } else {
            extractMediaTypeAndSpecialization(source, item);
        }
        extractAdditionalItemFields(source, item, now);
    }

    private boolean isAudioDescribed(com.metabroadcast.atlas.glycerin.model.Version nitroVersion) {
        VersionTypes versionTypes = nitroVersion.getVersionTypes();

        if (versionTypes == null) {
            return false;
        }

        return Iterables.any(versionTypes.getVersionType(), IS_AUDIO_DESCRIBED);
    }

    private void extractMediaTypeAndSpecialization(NitroItemSource<SOURCE> source, ITEM item) {
        String mediaType = extractMediaType(source);
        if (mediaType != null) {
            item.setMediaType(MediaType.fromKey(mediaType.toLowerCase()).orNull());
        }
        if (MediaType.VIDEO.equals(item.getMediaType())) {
            item.setSpecialization(Specialization.TV);
        } else if (MediaType.AUDIO.equals(item.getMediaType())) {
            item.setSpecialization(Specialization.RADIO);
        }
    }

    /**
     * Extract the media type of the source.
     * @param source
     * @return the media type of the source, or null if not present.
     */
    protected abstract String extractMediaType(NitroItemSource<SOURCE> source);

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

    private Set<Broadcast> completeBroadcastsWithVersion(
            ImmutableSetMultimap<String, Broadcast> broadcasts,
            com.metabroadcast.atlas.glycerin.model.Version nitroVersion) {

        return FluentIterable.from(broadcasts.get(nitroVersion.getPid()))
                .transform(completeBroadcast(nitroVersion))
                .toSet();
    }

    private Function<Broadcast, Broadcast> completeBroadcast(final com.metabroadcast.atlas.glycerin.model.Version version) {
        return new Function<Broadcast, Broadcast>() {
            @Override public Broadcast apply(Broadcast broadcast) {
                broadcast.setWidescreen(WIDESCREEN_RATIO.equals(version.getAspectRatio()));
                return broadcast;
            }
        };
    }

    private Duration convertDuration(javax.xml.datatype.Duration xmlDuration) {
        return Duration.standardSeconds(xmlDuration.getSeconds());
    }

    private Optional<WarningText> warningTextFrom(com.metabroadcast.atlas.glycerin.model.Version version) {
        Warnings warnings = version.getWarnings();

        if (warnings == null) {
            return Optional.absent();
        }

        return Iterables.tryFind(warnings.getWarningText(), IS_SHORT_WARNING);
    }
    
}
