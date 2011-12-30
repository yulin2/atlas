package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Restriction;
import org.atlasapi.media.entity.simple.SeriesSummary;
import org.atlasapi.media.segment.SegmentResolver;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.intl.Countries;

public class ItemModelSimplifier extends ContentModelSimplifier<Item, org.atlasapi.media.entity.simple.Item> {

    protected final CrewMemberSimplifier crewSimplifier = new CrewMemberSimplifier();
    private final ContentResolver contentResolver;
    private final SegmentModelSimplifier segmentSimplifier;
    
    public ItemModelSimplifier(ContentResolver contentResolver, TopicQueryResolver topicResolver, SegmentResolver segmentResolver) {
        super(topicResolver);
        this.contentResolver = contentResolver;
        this.segmentSimplifier = segmentResolver != null ? new SegmentModelSimplifier(segmentResolver) : null;
    }

    @Override
    public org.atlasapi.media.entity.simple.Item simplify(Item full, final Set<Annotation> annotations) {

        org.atlasapi.media.entity.simple.Item simple = new org.atlasapi.media.entity.simple.Item();

        copyProperties(full, simple, annotations);
        
        boolean doneSegments = false;
        for (Version version : full.getVersions()) {
            addTo(simple, version, full, annotations);
            if(!doneSegments && !version.getSegmentEvents().isEmpty() && annotations.contains(Annotation.SEGMENT_EVENTS) && segmentSimplifier != null) {
                simple.setSegments(segmentSimplifier.simplify(version.getSegmentEvents(), annotations));
                doneSegments = true;
            }
        }

        if (annotations.contains(Annotation.PEOPLE)) {
            simple.setPeople(Iterables.filter(Iterables.transform(full.people(), new Function<CrewMember, org.atlasapi.media.entity.simple.Person>() {
                @Override
                public org.atlasapi.media.entity.simple.Person apply(CrewMember input) {
                    return crewSimplifier.simplify(input, annotations);
                }
            }), Predicates.notNull()));
        }

        return simple;
    }

    private void copyProperties(Item fullItem, org.atlasapi.media.entity.simple.Item simpleItem, Set<Annotation> annotations) {
        copyBasicContentAttributes(fullItem, simpleItem, annotations);
        simpleItem.setType(EntityType.from(fullItem).toString());
        
        if (annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            simpleItem.setBlackAndWhite(fullItem.getBlackAndWhite());
            simpleItem.setCountriesOfOrigin(fullItem.getCountriesOfOrigin());
            simpleItem.setScheduleOnly(fullItem.isScheduleOnly());
        }

        if (fullItem.getContainer() != null) {
            simpleItem.setBrandSummary(summaryFromResolved(fullItem.getContainer(), annotations));
        }

        if (fullItem instanceof Episode) {
            Episode episode = (Episode) fullItem;

            if (annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
                ParentRef series = episode.getSeriesRef();
                if (series != null) {
                    simpleItem.setSeriesSummary(seriesSummaryFromResolved(series, annotations));
                }
            }

            if (annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
                simpleItem.setEpisodeNumber(episode.getEpisodeNumber());
                simpleItem.setSeriesNumber(episode.getSeriesNumber());
            }

        } else if (fullItem instanceof Film && (annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION))) {
            Film film = (Film) fullItem;
            simpleItem.setYear(film.getYear());
        }
    }

    private void addTo(org.atlasapi.media.entity.simple.Item simpleItem, Version version, Item item, Set<Annotation> annotations) {

        if (annotations.contains(Annotation.LOCATIONS)) {
            for (Encoding encoding : version.getManifestedAs()) {
                addTo(simpleItem, version, encoding, item);
            }
        }

        if (annotations.contains(Annotation.BROADCASTS)) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                if (broadcast.isActivelyPublished()) {
                    org.atlasapi.media.entity.simple.Broadcast simpleBroadcast = simplify(broadcast);
                    copyProperties(version, simpleBroadcast, item);
                    simpleItem.addBroadcast(simpleBroadcast);
                }
            }
        }
    }

    private org.atlasapi.media.entity.simple.Broadcast simplify(Broadcast broadcast) {
        org.atlasapi.media.entity.simple.Broadcast simpleModel = new org.atlasapi.media.entity.simple.Broadcast(broadcast.getBroadcastOn(), broadcast.getTransmissionTime(),
                broadcast.getTransmissionEndTime(), broadcast.getId());

        simpleModel.setRepeat(broadcast.getRepeat());
        simpleModel.setSubtitled(broadcast.getSubtitled());
        simpleModel.setSigned(broadcast.getSigned());
        simpleModel.setAudioDescribed(broadcast.getAudioDescribed());
        simpleModel.setHighDefinition(broadcast.getHighDefinition());
        simpleModel.setWidescreen(broadcast.getWidescreen());
        simpleModel.setSurround(broadcast.getSurround());
        simpleModel.setLive(broadcast.getLive());
        simpleModel.setPremiere(broadcast.getPremiere());
        simpleModel.setNewSeries(broadcast.getNewSeries());

        return simpleModel;
    }

    private void copyProperties(Version version, org.atlasapi.media.entity.simple.Version simpleLocation, org.atlasapi.media.entity.Item item) {

        simpleLocation.setPublishedDuration(version.getPublishedDuration());
        simpleLocation.setDuration(durationFrom(item, version));

        Restriction restriction = new Restriction();

        if (version.getRestriction() != null) {
            restriction.setRestricted(version.getRestriction().isRestricted());
            restriction.setMinimumAge(version.getRestriction().getMinimumAge());
            restriction.setMessage(version.getRestriction().getMessage());
        }

        simpleLocation.setRestriction(restriction);
    }

    // temporary fix: some versions are missing durations so
    // we fall back to the broadcast and location durations
    private Integer durationFrom(org.atlasapi.media.entity.Item item, Version version) {
        if (version.getDuration() != null && version.getDuration() > 0) {
            return version.getDuration();
        }
        Iterable<Broadcast> broadcasts = item.flattenBroadcasts();
        if (Iterables.isEmpty(broadcasts)) {
            return null;
        }
        return Ordering.natural().max(Iterables.transform(broadcasts, new Function<Broadcast, Integer>() {
            @Override
            public Integer apply(Broadcast input) {
                Integer duration = input.getBroadcastDuration();
                if (duration == null) {
                    return 0;
                }
                return duration;
            }
        }));
    }

    private void addTo(org.atlasapi.media.entity.simple.Item simpleItem, Version version, Encoding encoding, Item item) {
        for (Location location : encoding.getAvailableAt()) {
            addTo(simpleItem, version, encoding, location, item);
        }
    }

    private void addTo(org.atlasapi.media.entity.simple.Item simpleItem, Version version, Encoding encoding, Location location, Item item) {

        org.atlasapi.media.entity.simple.Location simpleLocation = new org.atlasapi.media.entity.simple.Location();

        copyProperties(version, simpleLocation, item);
        copyProperties(encoding, simpleLocation);
        copyProperties(location, simpleLocation);

        simpleItem.addLocation(simpleLocation);
    }

    private SeriesSummary seriesSummaryFromResolved(ParentRef seriesRef, Set<Annotation> annotations) {
        SeriesSummary seriesSummary = new SeriesSummary();
        seriesSummary.setUri(seriesRef.getUri());

        if (annotations.contains(Annotation.SERIES_SUMMARY)) {
            final Maybe<Identified> resolved = contentResolver.findByCanonicalUris(ImmutableList.of(seriesRef.getUri())).get(seriesRef.getUri());
            if (resolved.hasValue() && resolved.requireValue() instanceof Series) {
                Series series = (Series) resolved.requireValue();
                seriesSummary.setTitle(series.getTitle());
                seriesSummary.setDescription(series.getDescription());
                seriesSummary.setCurie(series.getCurie());
            }
        }
        
        return seriesSummary;
    }

    private BrandSummary summaryFromResolved(ParentRef container, Set<Annotation> annotations) {
        BrandSummary brandSummary = new BrandSummary();
        brandSummary.setUri(container.getUri());

        if (annotations.contains(Annotation.BRAND_SUMMARY)) {
            final Maybe<Identified> resolved = contentResolver.findByCanonicalUris(ImmutableList.of(container.getUri())).get(container.getUri());
            if (resolved.hasValue() && resolved.requireValue() instanceof Brand) {
                Brand brand = (Brand) resolved.requireValue();
                brandSummary.setTitle(brand.getTitle());
                brandSummary.setDescription(brand.getDescription());
                brandSummary.setCurie(brand.getCurie());
            }
        }
        
        return brandSummary;
    }

    private void copyProperties(Encoding encoding, org.atlasapi.media.entity.simple.Location simpleLocation) {

        simpleLocation.setAdvertisingDuration(encoding.getAdvertisingDuration());
        simpleLocation.setAudioBitRate(encoding.getAudioBitRate());
        simpleLocation.setAudioChannels(encoding.getAudioChannels());
        simpleLocation.setBitRate(encoding.getBitRate());
        simpleLocation.setContainsAdvertising(encoding.getContainsAdvertising());
        if (encoding.getDataContainerFormat() != null) {
            simpleLocation.setDataContainerFormat(encoding.getDataContainerFormat().toString());
        }
        simpleLocation.setDataSize(encoding.getDataSize());
        simpleLocation.setDistributor(encoding.getDistributor());
        simpleLocation.setHasDOG(encoding.getHasDOG());
        simpleLocation.setSource(encoding.getSource());
        simpleLocation.setVideoAspectRatio(encoding.getVideoAspectRatio());
        simpleLocation.setVideoBitRate(encoding.getVideoBitRate());

        if (encoding.getVideoCoding() != null) {
            simpleLocation.setVideoCoding(encoding.getVideoCoding().toString());
        }
        if (encoding.getAudioCoding() != null) {
            simpleLocation.setAudioCoding(encoding.getAudioCoding().toString());
        }

        simpleLocation.setVideoFrameRate(encoding.getVideoFrameRate());
        simpleLocation.setVideoHorizontalSize(encoding.getVideoHorizontalSize());
        simpleLocation.setVideoProgressiveScan(encoding.getVideoProgressiveScan());
        simpleLocation.setVideoVerticalSize(encoding.getVideoVerticalSize());
    }

    private void copyProperties(Location location, org.atlasapi.media.entity.simple.Location simpleLocation) {
        Policy policy = location.getPolicy();
        if (policy != null) {
            if (policy.getAvailabilityStart() != null) {
                simpleLocation.setAvailabilityStart(policy.getAvailabilityStart().toDate());
            }
            if (policy.getAvailabilityEnd() != null) {
                simpleLocation.setAvailabilityEnd(policy.getAvailabilityEnd().toDate());
            }
            if (policy.getDrmPlayableFrom() != null) {
                simpleLocation.setDrmPlayableFrom(policy.getDrmPlayableFrom().toDate());
            }
            if (policy.getAvailableCountries() != null) {
                simpleLocation.setAvailableCountries(Countries.toCodes(policy.getAvailableCountries()));
            }
            if (policy.getRevenueContract() != null) {
                simpleLocation.setRevenueContract(policy.getRevenueContract().key());
            }
            if (policy.getPrice() != null) {
                simpleLocation.setPrice(policy.getPrice().getAmount());
                simpleLocation.setCurrency(policy.getPrice().getCurrency().getCurrencyCode());
            }
            if (policy.getPlatform() != null) {
                simpleLocation.setPlatform(policy.getPlatform().key());
            }
        }

        simpleLocation.setTransportIsLive(location.getTransportIsLive());
        if (location.getTransportType() != null) {
            simpleLocation.setTransportType(location.getTransportType().toString());
        }
        if (location.getTransportSubType() != null) {
            simpleLocation.setTransportSubType(location.getTransportSubType().toString());
        }
        simpleLocation.setUri(location.getUri());
        simpleLocation.setEmbedCode(location.getEmbedCode());
        simpleLocation.setEmbedId(location.getEmbedId());

    }
}
