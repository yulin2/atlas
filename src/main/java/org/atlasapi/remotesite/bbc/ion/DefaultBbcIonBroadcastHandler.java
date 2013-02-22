package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
import static org.atlasapi.remotesite.bbc.BbcFeeds.slashProgrammesUriForPid;

import java.util.List;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ContentLock;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class DefaultBbcIonBroadcastHandler implements BbcIonBroadcastHandler {

    private static final String BBC_CURIE_BASE = "bbc:";
    
    private final ContentStore store;
    private final AdapterLog log;

    private final BbcIonEpisodeItemContentExtractor itemExtractor;
    private BbcIonBroadcastExtractor broadcastExtractor;
    
    private final BbcIonItemMerger merger = new BbcIonItemMerger();
    
    private SiteSpecificAdapter<Item> itemClient;
    private BbcContainerAdapter containerClient;
    private ItemsPeopleWriter itemsPeopleWriter;
    private SiteSpecificAdapter<List<SegmentEvent>> segmentAdapter;

    private final ContentLock lock;

    
    public DefaultBbcIonBroadcastHandler(ContentStore store, AdapterLog log, ContentLock lock) {
        this.store = store;
        this.log = log;
        this.lock = lock;
        this.itemExtractor = new BbcIonEpisodeItemContentExtractor(log);
        this.broadcastExtractor = new BbcIonBroadcastExtractor();
    }

    public DefaultBbcIonBroadcastHandler withItemFetcherClient(SiteSpecificAdapter<Item> client) {
        this.itemClient = client;
        return this;
    }

    public DefaultBbcIonBroadcastHandler withContainerFetcherClient(BbcContainerAdapter containerClient) {
        this.containerClient = containerClient;
        return this;
    }

    public DefaultBbcIonBroadcastHandler withItemPeopleWriter(ItemsPeopleWriter itemsPeopleWriter) {
        this.itemsPeopleWriter = itemsPeopleWriter;
        return this;
    }
    
    public DefaultBbcIonBroadcastHandler withSegmentAdapter(SiteSpecificAdapter<List<SegmentEvent>> segmentAdapter) {
        this.segmentAdapter = segmentAdapter;
        return this;
    }

    @Override
    public Maybe<ItemAndBroadcast> handle(IonBroadcast ionBroadcast) {
        String itemUri = slashProgrammesUriForPid(ionBroadcast.getEpisodeId());
        try {
            lock.lock(itemUri);
            Item item = resolveOrFetchItem(ionBroadcast, itemUri);
                
            if(item == null) {
                return Maybe.nothing();
            }
                
            //ensure broadcast is included.
            Maybe<Broadcast> broadcast = addBroadcastToItem(item, ionBroadcast);

            addSegmentEventsToItemIfPermitted(item, ionBroadcast);
            String canonicalUri = item.getCanonicalUri();
            
            Brand brand = !Strings.isNullOrEmpty(ionBroadcast.getBrandId()) ? getOrCreateBrand(ionBroadcast, canonicalUri) : null;
            Series series = !Strings.isNullOrEmpty(ionBroadcast.getSeriesId()) ? getOrCreateSeries(ionBroadcast, canonicalUri) : null;

            if (brand != null) {
                updateBrand(brand, ionBroadcast);
                item.setContainer(brand);
                store.writeContent(brand);
            }

            if (series != null) {
                updateSeries(series, ionBroadcast);
                updateEpisodeSeriesDetails(series, (Episode) item);
                if (brand != null) {
                    series.setParent(brand);
                } else {
                    item.setContainer(series);
                    series.setParentRef(null);
                }
                store.writeContent(series);
                ((Episode) item).setSeries(series);
            }

            store.writeContent(item);
            createOrUpdatePeople((Item) item);
            
            return Maybe.just(new ItemAndBroadcast(item, broadcast));

        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass())
                    .withDescription("Schedule Updater failed for %s %s, processing broadcast %s of %s", ionBroadcast.getService(), ionBroadcast.getDate(), ionBroadcast.getId(), itemUri));
        } finally {
            lock.unlock(itemUri);
        }
        return Maybe.nothing();
    }

    private void addSegmentEventsToItemIfPermitted(Item item, IonBroadcast ionBroadcast) {
        if(segmentFetchPermitted(ionBroadcast, item.getCanonicalUri())) {
            Version version = getBroadcastVersion(item, ionBroadcast);
            version.setSegmentEvents(segmentAdapter.fetch(version.getCanonicalUri()));
        }
        
    }

    private Item resolveOrFetchItem(IonBroadcast broadcast, String itemUri) {
        Item item = null;
        
        //Get basic item from latest remote data.
        Item basicItem = itemExtractor.extract(broadcast.getEpisode()); 

        // look for existing item, merge into latest remote data, else fetch complete.
        Optional<Content> possibleIdentified = store.resolveAliases(ImmutableList.of(itemUri), BBC).get(itemUri);
        if (possibleIdentified.isPresent()) {
            Identified ided = possibleIdentified.get();
            if (!(ided instanceof Item)) {
                log.record(new AdapterLogEntry(Severity.WARN).withDescription("Updating %s, expecting Item, got %s", itemUri, ided.getClass().getSimpleName()).withSource(getClass()));
                return null;
            }
            item = merger.merge(fetchedFullItemIfPermitted(broadcast, itemUri).or(basicItem), (Item)ided);
        } else {
            item = fetchFullItem(itemUri).or(basicItem);
        }
        return item;
    }

    private Optional<Item> fetchedFullItemIfPermitted(IonBroadcast broadcast, String itemUri) {
        return fullFetchPermitted(broadcast, itemUri) ? fetchFullItem(itemUri) : Optional.<Item>absent();
    }

    protected boolean fullFetchPermitted(IonBroadcast broadcast, String itemUri) {
        return false;
    }

    private Optional<Item> fetchFullItem(String itemUri) {
        Item fetchedItem = null;
        if (itemClient != null) {
            try {
                fetchedItem = itemClient.fetch(itemUri);
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Failed to fetch ", itemUri));
            }
        }
        return Optional.fromNullable(fetchedItem);
    }

//    private void merge(Item fetchedItem, Item existing) {
//        //merge into fetched item to ensure type is up to date, need to be careful not to overwrite new data then.
//        fetchedItem.addAliases(existing.getAliases());
//        fetchedItem.setGenres(Iterables.concat(fetchedItem.getGenres(), existing.getGenres()));
//        fetchedItem.setTags(ImmutableSet.copyOf(Iterables.concat(fetchedItem.getTags(), existing.getTags())));
//        fetchedItem.setFirstSeen(existing.getFirstSeen());
//        fetchedItem.setPeople(existing.getPeople());
//        fetchedItem.setClips(Iterables.concat(fetchedItem.getClips(),existing.getClips()));
//        fetchedItem.setVersions(ImmutableSet.copyOf(Iterables.concat(fetchedItem.getVersions(), existing.getVersions())));
//    }

    private void updateEpisodeSeriesDetails(Series series, Episode episode) {
        Integer seriesNumber = series.getSeriesNumber();
        if (seriesNumber != null && seriesNumber > 0) {
            episode.setSeriesNumber(seriesNumber);
        }
        episode.setSeries(series);
    }

    private Series getOrCreateSeries(IonBroadcast broadcast, String itemUri) {
        String seriesUri = slashProgrammesUriForPid(broadcast.getSeriesId());
        Optional<Content> maybeSeries = store.resolveAliases(ImmutableList.of(seriesUri), BBC).get(seriesUri);

        if (!maybeSeries.isPresent()) {
            Maybe<Series> series = Maybe.nothing();
            if (containerClient != null) {
                series = containerClient.createSeries(broadcast.getSeriesId());
            }
            return series.hasValue() ? series.requireValue() : createSeries(broadcast);
        }

        Identified ided = maybeSeries.get();
        if (ided instanceof Series) {
            return (Series) ided;
        }

        log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Updating item %s, got %s when resolving for Series %s", itemUri, ided.getClass().getSimpleName(), seriesUri));
        return null;
    }

    private Brand getOrCreateBrand(IonBroadcast broadcast, String itemUri) {
        String brandUri = slashProgrammesUriForPid(broadcast.getBrandId());

        Optional<Content> maybeIdentified = store.resolveAliases(ImmutableList.of(brandUri), BBC).get(brandUri);

        if (!maybeIdentified.isPresent()) {
            Maybe<Brand> brand = Maybe.nothing();
            if (containerClient != null) {
                brand = containerClient.createBrand(broadcast.getBrandId());
            }
            return brand.hasValue() ? brand.requireValue() : createBrandFrom(broadcast);
        }

        Identified ided = maybeIdentified.get();
        if (ided instanceof Brand) {
            Brand brand = (Brand) ided;
            if(brand.getGenres().isEmpty()) {
                // genres were not being ingested previously, so we should go get them if we don't 
                // have any for this brand
                Maybe<Brand> fetchedBrand = containerClient.createBrand(broadcast.getBrandId());
                if(fetchedBrand.hasValue()) {
                    brand.setGenres(fetchedBrand.requireValue().getGenres());
                }
            }
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
        return new Series(slashProgrammesUriForPid(broadcast.getSeriesId()), BBC_CURIE_BASE + broadcast.getSeriesId(), Publisher.BBC);
    }

    private Brand createBrandFrom(IonBroadcast broadcast) {
        return new Brand(slashProgrammesUriForPid(broadcast.getBrandId()), BBC_CURIE_BASE + broadcast.getBrandId(), Publisher.BBC);
    }

    private void updateBrand(Brand brand, IonBroadcast broadcast) {
        brand.setTitle(broadcast.getEpisode().getBrandTitle());
        BbcImageUrlCreator.addImagesTo("http://www.bbc.co.uk/iplayer/images/progbrand/", broadcast.getBrandId(), brand);
    }

    private Maybe<Broadcast> addBroadcastToItem(Item item, IonBroadcast ionBroadcast) {
        Version broadcastVersion = getBroadcastVersion(item, ionBroadcast);
        if (broadcastVersion == null) {
            broadcastVersion = versionFrom(ionBroadcast);
            item.addVersion(broadcastVersion);
        }

        Maybe<Broadcast> broadcast = broadcastExtractor.extract(ionBroadcast);
        if (broadcast.hasValue()) {
            broadcastVersion.addBroadcast(broadcast.requireValue());
        } else {
            log.record(AdapterLogEntry.warnEntry().withSource(getClass()).withDescription("Couldn't find service URI for Ion Service %s", ionBroadcast.getService()));
        }
        return broadcast;
    }

    private Version versionFrom(IonBroadcast ionBroadcast) {
        Version version = new Version();

        version.setCanonicalUri(slashProgrammesUriForPid(ionBroadcast.getVersionId()));
        version.setDuration(Duration.standardSeconds(ionBroadcast.getDuration()));
        version.setProvider(Publisher.BBC);

        return version;
    }

    private Version getBroadcastVersion(Item item, IonBroadcast ionBroadcast) {
        String versionUri = BbcFeeds.slashProgrammesUriForPid(ionBroadcast.getVersionId());
        for (Version version : item.nativeVersions()) {
            if (version.getCanonicalUri().equals(versionUri)) {
                return version;
            }
        }
        return null;
    }

    protected boolean segmentFetchPermitted(IonBroadcast broadcast, String itemUri) {
        return this.segmentAdapter != null;
    }

}
