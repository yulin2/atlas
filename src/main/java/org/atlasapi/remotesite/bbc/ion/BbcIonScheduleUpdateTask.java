package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.DEBUG;
import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;
import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

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
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.SimpleHttpClient;

public class BbcIonScheduleUpdateTask implements Runnable {

    private static final String BBC_CURIE_BASE = "bbc:";
    private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
    public  static final String SCHEDULE_PATTERN = "http://www.bbc.co.uk/iplayer/ion/schedule/service/%s/date/%s/timeslot/day/format/json";
    
    private final SimpleHttpClient httpClient;
    private final ContentResolver localFetcher;
    private final ContentWriter writer;
    private final AdapterLog log;

    private final BbcIonDeserializer<IonSchedule> deserialiser = deserializerForClass(IonSchedule.class);
    
    private ItemsPeopleWriter itemsPeopleWriter;
    private BbcItemFetcherClient itemClient;
    private BbcContainerFetcherClient containerClient;
    
    private final String serviceKey;
    private final LocalDate day;

    public BbcIonScheduleUpdateTask(String serviceKey, LocalDate day, SimpleHttpClient httpClient, ContentResolver localFetcher, ContentWriter writer, AdapterLog log){
        this.serviceKey = serviceKey;
        this.day = day;
        this.httpClient = httpClient;
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.log = log;
    }
    
    public BbcIonScheduleUpdateTask withItemFetcherClient(BbcItemFetcherClient client) {
        this.itemClient = client;
        return this;
    }
    
    public BbcIonScheduleUpdateTask withContainerFetcherClient(BbcContainerFetcherClient containerClient) {
        this.containerClient = containerClient;
        return this;
    }
    
    public BbcIonScheduleUpdateTask withItemPeopleWriter(ItemsPeopleWriter itemsPeopleWriter) {
        this.itemsPeopleWriter = itemsPeopleWriter;
        return this;
    }

    @Override
    public void run() {
        String uri = String.format(SCHEDULE_PATTERN, serviceKey, day.toString("yyyyMMdd"));
        log.record(new AdapterLogEntry(DEBUG).withSource(getClass()).withDescription("BBC Ion Schedule update for " + uri));
        
        IonSchedule schedule;
        try {
            schedule = deserialiser.deserialise(httpClient.getContentsOf(uri));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri).withSource(getClass()));
            return;
        }
        
        for (IonBroadcast broadcast : schedule.getBlocklist()) {
            String itemUri = SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId();
            try {
                // find and (create and) update item
                Identified ided = findOrCreateItem(broadcast, itemUri);

                if(!(ided instanceof Item)) {
                    log.record(new AdapterLogEntry(Severity.WARN).withDescription(
                            String.format("Updating item %s, got %s when looking for Item", itemUri, ided.getClass().getSimpleName())
                    ).withSource(getClass()));
                    return;
                }
                
                Item item = (Item) ided;
                
                updateItemDetails(item, broadcast);

                String canonicalUri = item.getCanonicalUri();
                
                if (item instanceof Episode) {
                    updateEpisodeDetails((Episode) item, broadcast);
                } else if (hasEpisodeDetails(broadcast.getEpisode())) {
                    log.record(new AdapterLogEntry(Severity.INFO).withDescription(
                            String.format("Updating Episode %s, resolved %s", canonicalUri, item.getClass().getSimpleName())
                    ).withSource(getClass()));
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
                    updateEpisodeSeriesDetails(series, (Episode)item);
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

            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri + " trying to process broadcast for " + itemUri).withSource(getClass()));
            }
        }
    }

    private void updateEpisodeSeriesDetails(Series series, Episode episode) {
        Integer seriesNumber = series.getSeriesNumber();
        if(seriesNumber != null && seriesNumber > 0) {
            episode.setSeriesNumber(seriesNumber);
        }
        episode.setSeries(series);
    }

    private Series getOrCreateSeries(IonBroadcast broadcast, String itemCanonicalUri) {
        String seriesUri = SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId();
        Maybe<Identified> maybeSeries = localFetcher.findByCanonicalUris(ImmutableList.of(seriesUri)).get(seriesUri);

        if (maybeSeries.isNothing()) {
            Maybe<Series> series = Maybe.nothing();
            if(containerClient != null) {
                series = containerClient.createSeries(broadcast.getSeriesId());
            }
            return series.hasValue() ? series.requireValue() : createSeries(broadcast);
        }
        
        Identified ided = maybeSeries.requireValue();
        if (ided instanceof Series) {
            return (Series) ided;
        }

        log.record(new AdapterLogEntry(Severity.WARN).withDescription(
                String.format("Updating item %s, got %s when resolving for Series %s", itemCanonicalUri, ided.getClass().getSimpleName(), seriesUri)
        ).withSource(getClass()));
        return null;
    }

    private Brand getOrCreateBrand(IonBroadcast broadcast, String itemCanonicalUri) {
        String brandUri = SLASH_PROGRAMMES_ROOT + broadcast.getBrandId();
        
        Maybe<Identified> maybeIdentified = localFetcher.findByCanonicalUris(ImmutableList.of(brandUri)).get(brandUri);

        if (maybeIdentified.isNothing()) {
            Maybe<Brand> brand = Maybe.nothing();
            if(containerClient != null) {
                brand = containerClient.createBrand(broadcast.getSeriesId());
            }
            return brand.hasValue() ? brand.requireValue() : createBrandFrom(broadcast);
        }
        
        Identified ided = maybeIdentified.requireValue();
        if (ided instanceof Brand) {
            return (Brand) ided;
        } else if (ided instanceof Series) {
            Brand brand = brandFromSeries((Series) ided);
            log.record(new AdapterLogEntry(Severity.INFO).withDescription(
                    String.format("Updating item %s, got Series when looking for Brand %s, converted it to Brand", itemCanonicalUri, brandUri)
            ).withSource(getClass()));
            return brand;
        } 
        
        log.record(new AdapterLogEntry(Severity.WARN).withDescription(
                String.format("Updating item %s, got %s when resolving for Brand %s", itemCanonicalUri, ided.getClass().getSimpleName(), brandUri)
        ).withSource(getClass()));
        return null;
    }

    private Identified findOrCreateItem(IonBroadcast broadcast, String itemUri) {
        Maybe<Identified> possibleIdentified = localFetcher.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
        if (possibleIdentified.isNothing()) {
            if(itemClient != null) {
                Item fetchedItem = itemClient.createItem(broadcast.getEpisodeId());
                if(fetchedItem != null) {
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
        if(maybeMediaType.hasValue()) {
            item.setMediaType(maybeMediaType.requireValue());
        } 
        
        Maybe<Specialization> maybeSpecialisation = BbcIonMediaTypeMapping.specialisationForService(ionEpisode.getMasterbrand());
        if(maybeSpecialisation.hasValue()) {
            item.setSpecialization(maybeSpecialisation.requireValue());
        }
        
        if (ionEpisode.getIsFilm() != null && ionEpisode.getIsFilm()) {
            item.setSpecialization(Specialization.FILM);
        }
        return item;
    }
    
    private void createOrUpdatePeople(Item item) {
        if(itemsPeopleWriter != null) {
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
            item.setEpisodeNumber(Ints.saturatedCast(episode.getPosition()));
        }
    }
    
    private boolean hasEpisodeDetails(IonEpisode episode) {
        return episode != null && Strings.isNullOrEmpty(episode.getSubseriesId()) && episode.getPosition() != null;
    }

    private void updateItemDetails(Item item, IonBroadcast ionBroadcast) {

        Version broadcastVersion = getBroadcastVersion(item, ionBroadcast);
        if (broadcastVersion == null) {
            broadcastVersion = versionFrom(ionBroadcast);
            item.addVersion(broadcastVersion);
        }
        
        Broadcast broadcast = atlasBroadcastFrom(ionBroadcast);
        if(broadcast!= null) {
            broadcastVersion.addBroadcast(broadcast);
        }

        IonEpisode episode = ionBroadcast.getEpisode();
        item.setTitle(episode.getTitle());
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
        if(serviceUri == null) {
            log.record(new AdapterLogEntry(WARN).withDescription("Couldn't find service URI for Ion Service " + ionBroadcast.getService()).withSource(getClass()));
            return null;
        } else {
            Broadcast broadcast = new Broadcast(serviceUri, ionBroadcast.getStart(), ionBroadcast.getEnd());
            broadcast.withId(BBC_CURIE_BASE + ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
            broadcast.setLastUpdated(ionBroadcast.getUpdated());
            return broadcast;
        }
    }
}
