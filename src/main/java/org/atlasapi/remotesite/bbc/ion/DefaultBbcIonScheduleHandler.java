package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.bbc.BbcAliasCompiler;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonContainer;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.Duration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.base.Maybe;

public class DefaultBbcIonScheduleHandler implements BbcIonScheduleHandler {

    private static final String BBC_CURIE_BASE = "bbc:";
    private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final AdapterLog log;
    
    private BbcItemFetcherClient itemClient;
    private BbcContainerFetcherClient containerClient;
    private ItemsPeopleWriter itemsPeopleWriter;
    
    public DefaultBbcIonScheduleHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        this.resolver = resolver;
        this.writer = writer;
        this.log = log;
    }

    public DefaultBbcIonScheduleHandler withItemFetcherClient(BbcItemFetcherClient client) {
        this.itemClient = client;
        return this;
    }

    public DefaultBbcIonScheduleHandler withContainerFetcherClient(BbcContainerFetcherClient containerClient) {
        this.containerClient = containerClient;
        return this;
    }

    public DefaultBbcIonScheduleHandler withItemPeopleWriter(ItemsPeopleWriter itemsPeopleWriter) {
        this.itemsPeopleWriter = itemsPeopleWriter;
        return this;
    }

    @Override
    public int handle(IonSchedule schedule) {
        int processed = 0;

        for (IonBroadcast broadcast : schedule.getBlocklist()) {
            String itemUri = SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId();
            try {
                // find and (create and) update item
                Identified ided = findOrCreateItem(broadcast, itemUri);

                if (!(ided instanceof Item)) {
                    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Updating item %s, got %s when looking for Item", itemUri, ided.getClass().getSimpleName()).withSource(getClass()));
                    continue;
                }

                Item item = (Item) ided;

                updateItemDetails(item, broadcast);

                String canonicalUri = item.getCanonicalUri();

                if (item instanceof Episode) {
                    updateEpisodeDetails((Episode) item, broadcast);
                } else if (hasEpisodeDetails(broadcast.getEpisode())) {
                    log.record(new AdapterLogEntry(Severity.INFO).withDescription(String.format("Updating Episode %s, resolved %s", canonicalUri, item.getClass().getSimpleName())).withSource(
                            getClass()));
                }

                Brand brand = !Strings.isNullOrEmpty(broadcast.getBrandId()) ? getOrCreateBrand(broadcast, canonicalUri) : null;
                Series series = !Strings.isNullOrEmpty(broadcast.getSeriesId()) ? getOrCreateSeries(broadcast, canonicalUri) : null;

                if (brand != null) {
                    updateBrand(brand, broadcast);
                    item.setContainer(brand);
                    writer.createOrUpdate(brand);
                }

                if (series != null) {
                    updateSeries(series, broadcast);
                    updateEpisodeSeriesDetails(series, (Episode) item);
                    if (brand != null) {
                        series.setParent(brand);
                    } else {
                        item.setContainer(series);
                    }
                    writer.createOrUpdate(series);
                    ((Episode) item).setSeries(series);
                }

                writer.createOrUpdate(item);
                createOrUpdatePeople((Item) item);

                processed++;
            } catch (Exception e) {
                log.record(errorEntry().withCause(e).withSource(getClass())
                        .withDescription("Schedule Updater failed for %s %s, processing broadcast %s of %s", broadcast.getService(), broadcast.getDate(), broadcast.getId(), itemUri));
            }
        }

        return processed;
    }

    private void updateEpisodeSeriesDetails(Series series, Episode episode) {
        Integer seriesNumber = series.getSeriesNumber();
        if (seriesNumber != null && seriesNumber > 0) {
            episode.setSeriesNumber(seriesNumber);
        }
        episode.setSeries(series);
    }

    private Series getOrCreateSeries(IonBroadcast broadcast, String itemUri) {
        String seriesUri = SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId();
        Maybe<Identified> maybeSeries = resolver.findByCanonicalUris(ImmutableList.of(seriesUri)).get(seriesUri);

        if (maybeSeries.isNothing()) {
            Maybe<Series> series = Maybe.nothing();
            if (containerClient != null) {
                series = containerClient.createSeries(broadcast.getSeriesId());
            }
            return series.hasValue() ? series.requireValue() : createSeries(broadcast);
        }

        Identified ided = maybeSeries.requireValue();
        if (ided instanceof Series) {
            return (Series) ided;
        }

        log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription(
                String.format("Updating item %s, got %s when resolving for Series %s", itemUri, ided.getClass().getSimpleName(), seriesUri)));
        return null;
    }

    private Brand getOrCreateBrand(IonBroadcast broadcast, String itemUri) {
        String brandUri = SLASH_PROGRAMMES_ROOT + broadcast.getBrandId();

        Maybe<Identified> maybeIdentified = resolver.findByCanonicalUris(ImmutableList.of(brandUri)).get(brandUri);

        if (maybeIdentified.isNothing()) {
            Maybe<Brand> brand = Maybe.nothing();
            if (containerClient != null) {
                brand = containerClient.createBrand(broadcast.getBrandId());
            }
            return brand.hasValue() ? brand.requireValue() : createBrandFrom(broadcast);
        }

        Identified ided = maybeIdentified.requireValue();
        if (ided instanceof Brand) {
            return (Brand) ided;
        } else if (ided instanceof Series) {
            Brand brand = brandFromSeries((Series) ided); //Handle remote conversion of series to brand
            log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription(
                    String.format("Updating item %s, got Series when looking for Brand %s, converted it to Brand", itemUri, brandUri)));
            return brand;
        }

        log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription(
                String.format("Updating item %s, got %s when resolving for Brand %s", itemUri, ided.getClass().getSimpleName(), brandUri)));
        return null;
    }

    private Identified findOrCreateItem(IonBroadcast broadcast, String itemUri) {
        Maybe<Identified> possibleIdentified = resolver.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
        if (possibleIdentified.isNothing()) {
            if (itemClient != null) {
                Item fetchedItem = itemClient.createItem(broadcast.getEpisodeId());
                if (fetchedItem != null) {
                    return fetchedItem;
                }
            }
            return createItemFrom(broadcast);
        }
        return possibleIdentified.requireValue();
    }

    private Item createItemFrom(IonBroadcast broadcast) {
        IonEpisode ionEpisode = broadcast.getEpisode();

        Item item;
        if (!Strings.isNullOrEmpty(broadcast.getBrandId()) || !Strings.isNullOrEmpty(broadcast.getSeriesId())) {
            item = new Episode(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE + ionEpisode.getId(), Publisher.BBC);
        } else {
            item = new Item(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE + ionEpisode.getId(), Publisher.BBC);
        }

        item.setAliases(BbcAliasCompiler.bbcAliasUrisFor(item.getCanonicalUri()));
        item.setIsLongForm(true);

        if (!Strings.isNullOrEmpty(broadcast.getMediaType())) {
            item.setMediaType(MediaType.valueOf(broadcast.getMediaType().toUpperCase()));
        }

        Maybe<MediaType> maybeMediaType = BbcIonMediaTypeMapping.mediaTypeForService(ionEpisode.getMasterbrand());
        if (maybeMediaType.hasValue()) {
            item.setMediaType(maybeMediaType.requireValue());
        }

        Maybe<Specialization> maybeSpecialisation = BbcIonMediaTypeMapping.specialisationForService(ionEpisode.getMasterbrand());
        if (maybeSpecialisation.hasValue()) {
            item.setSpecialization(maybeSpecialisation.requireValue());
        }

        if (ionEpisode.getIsFilm() != null && ionEpisode.getIsFilm()) {
            item.setSpecialization(Specialization.FILM);
        }
        return item;
    }

    private void createOrUpdatePeople(Item item) {
        if (itemsPeopleWriter != null) {
            itemsPeopleWriter.createOrUpdatePeople(item);
        }
    }

    private Brand brandFromSeries(Series series) {
        Brand brand = new Brand(series.getCanonicalUri(), series.getCurie(), series.getPublisher());
        brand.setEquivalentTo(series.getEquivalentTo());
        brand.setTitle(series.getTitle());
        brand.setDescription(series.getDescription());
        brand.setImage(series.getImage());
        brand.setThumbnail(series.getThumbnail());
        brand.setFirstSeen(series.getFirstSeen());
        brand.setGenres(series.getGenres());
        brand.setMediaType(series.getMediaType());
        brand.setSpecialization(series.getSpecialization());

        return brand;
    }

    private void updateSeries(Series series, IonBroadcast broadcast) {
        series.setTitle(broadcast.getEpisode().getSeriesTitle());
    }

    private Series createSeries(IonBroadcast broadcast) {
        return new Series(SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId(), BBC_CURIE_BASE + broadcast.getSeriesId(), Publisher.BBC);
    }

    private Brand createBrandFrom(IonBroadcast broadcast) {
        return new Brand(SLASH_PROGRAMMES_ROOT + broadcast.getBrandId(), BBC_CURIE_BASE + broadcast.getBrandId(), Publisher.BBC);
    }

    private void updateBrand(Brand brand, IonBroadcast broadcast) {
        brand.setTitle(broadcast.getEpisode().getBrandTitle());
        BbcIonEpisodeDetailItemFetcherClient.addImagesTo("http://www.bbc.co.uk/iplayer/images/progbrand/", broadcast.getBrandId(), brand);
    }

    private void updateEpisodeDetails(Episode item, IonBroadcast broadcast) {
        IonEpisode episode = broadcast.getEpisode();
        if (hasEpisodeDetails(episode)) {

            String subseriesId = episode.getSubseriesId();

            if (Strings.isNullOrEmpty(subseriesId)) {
                item.setEpisodeNumber(Ints.saturatedCast(episode.getPosition()));
                return;
            }

            if (item.getPartNumber() == null && containerClient != null) {
                Maybe<IonContainer> subseries = containerClient.getSubseries(subseriesId);
                if (subseries.isNothing()) {
                    log.record(warnEntry().withSource(getClass()).withDescription("Updating item %s, couldn't fetch subseries %s", subseriesId));
                    return;
                }
                IonContainer subseriesContainer = subseries.requireValue();

                item.setEpisodeNumber(Ints.saturatedCast(subseriesContainer.getPosition()));
                item.setPartNumber(Ints.saturatedCast(episode.getPosition()));

            }

        }
    }

    private boolean hasEpisodeDetails(IonEpisode episode) {
        return episode != null && episode.getPosition() != null;
    }

    private void updateItemDetails(Item item, IonBroadcast ionBroadcast) {

        Version broadcastVersion = getBroadcastVersion(item, ionBroadcast);
        if (broadcastVersion == null) {
            broadcastVersion = versionFrom(ionBroadcast);
            item.addVersion(broadcastVersion);
        }

        Broadcast broadcast = atlasBroadcastFrom(ionBroadcast);
        if (broadcast != null) {
            broadcastVersion.addBroadcast(broadcast);
        }

        IonEpisode episode = ionBroadcast.getEpisode();

        String title = !Strings.isNullOrEmpty(episode.getOriginalTitle()) ? episode.getOriginalTitle() : episode.getTitle();
        if (!Strings.isNullOrEmpty(episode.getSubseriesTitle())) {
            title = String.format("%s %s", episode.getSubseriesTitle(), title);
        }
        item.setTitle(title);

        item.setDescription(episode.getSynopsis());
        item.setThumbnail(episode.getMyImageBaseUrl() + episode.getId() + "_150_84.jpg");
        item.setImage(episode.getMyImageBaseUrl() + episode.getId() + "_640_360.jpg");

    }

    private Version versionFrom(IonBroadcast ionBroadcast) {
        Version version = new Version();

        version.setCanonicalUri(SLASH_PROGRAMMES_ROOT + ionBroadcast.getVersionId());
        version.setDuration(Duration.standardSeconds(ionBroadcast.getDuration()));
        version.setProvider(Publisher.BBC);

        return version;
    }

    private Version getBroadcastVersion(Item item, IonBroadcast ionBroadcast) {
        for (Version version : item.nativeVersions()) {
            if (version.getCanonicalUri().equals(SLASH_PROGRAMMES_ROOT + ionBroadcast.getVersionId())) {
                return version;
            }
        }
        return null;
    }

    private Broadcast atlasBroadcastFrom(IonBroadcast ionBroadcast) {
        String serviceUri = BbcIonServices.get(ionBroadcast.getService());
        if (serviceUri == null) {
            log.record(new AdapterLogEntry(WARN).withSource(getClass()).withDescription("Couldn't find service URI for Ion Service %s", ionBroadcast.getService()));
            return null;
        } else {
            Broadcast broadcast = new Broadcast(serviceUri, ionBroadcast.getStart(), ionBroadcast.getEnd());
            broadcast.withId(BBC_CURIE_BASE + ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
            broadcast.setLastUpdated(ionBroadcast.getUpdated());
            return broadcast;
        }
    }
}
