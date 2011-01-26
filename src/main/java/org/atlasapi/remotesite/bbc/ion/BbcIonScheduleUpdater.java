package org.atlasapi.remotesite.bbc.ion;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.BbcAliasCompiler;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.Duration;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.metabroadcast.common.http.SimpleHttpClient;

public class BbcIonScheduleUpdater implements Runnable {

    private final Iterable<String> uriSource;
    private final ContentResolver localFetcher;
    private final AdapterLog log;

    private final DefinitiveContentWriter writer;
    private final BbcIonScheduleDeserialiser deserialiser;

    public BbcIonScheduleUpdater(Iterable<String> uriSource, ContentResolver localFetcher, DefinitiveContentWriter writer, BbcIonScheduleDeserialiser deserialiser, AdapterLog log) {
        this.uriSource = uriSource;
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.deserialiser = deserialiser;
        this.log = log;
    }

    @Override
    public void run() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update initiated"));

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (String uri : uriSource) {
            executor.submit(new BbcIonScheduleUpdateTask(uri,HttpClients.webserviceClient(), localFetcher, writer,deserialiser,log));
        }
        executor.shutdown();
        boolean completion = false;
        try {
            completion = executor.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("BBC Ion Schedule Update interrupted waiting for completion").withCause(e));
        }

        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update finished" + (completion ? "" : " (timed-out)")));
    }

    public static class BbcIonScheduleUpdateTask implements Runnable {

        private static final String BBC_CURIE_BASE = "bbc:";
        private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
        private final String uri;
        private final SimpleHttpClient httpClient;
        private final ContentResolver localFetcher;
        private DefinitiveContentWriter writer;
        private final AdapterLog log;
        private final BbcIonScheduleDeserialiser deserialiser;

        public BbcIonScheduleUpdateTask(String uri, SimpleHttpClient httpClient, ContentResolver localFetcher, DefinitiveContentWriter writer, BbcIonScheduleDeserialiser deserialiser, AdapterLog log){
            this.uri = uri;
            this.httpClient = httpClient;
            this.localFetcher = localFetcher;
            this.writer = writer;
            this.deserialiser = deserialiser;
            this.log = log;
        }

        @Override
        public void run() {
            try {
                IonSchedule schedule = deserialiser.deserialise(httpClient.getContentsOf(uri));
                for (IonBroadcast broadcast : schedule.getBlocklist()) {
                    //find and (create and) update item
                    Item item = (Item) localFetcher.findByUri(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId());
                    if (item == null) {
                        item = createItemFrom(broadcast);
                    }
                    updateItemDetails(item, broadcast);
                    if (item instanceof Episode) {
                        updateEpisodeDetails((Episode) item, broadcast);
                    }
                    //if no series and no brand just store item
                    if(Strings.isNullOrEmpty(broadcast.getSeriesId()) && Strings.isNullOrEmpty(broadcast.getBrandId())) {
                        writer.createOrUpdateItem(item);
                    } else {
                        Series series = null;
                        if (!Strings.isNullOrEmpty(broadcast.getSeriesId())) {
                            series = (Series) localFetcher.findByUri(SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId());
                            if (series == null) {
                                series = createSeries(broadcast);
                            }
                            updateSeries(series, broadcast);
                            addOrReplaceItemInPlaylist(item, series);
                        }
                        if (Strings.isNullOrEmpty(broadcast.getBrandId())) {
                            if(series != null) { //no brand so just save series if it exists
                                writer.createOrUpdateDefinitivePlaylist(series);
                            }
                        } else {
                            Brand brand = (Brand) localFetcher.findByUri(SLASH_PROGRAMMES_ROOT + broadcast.getBrandId());
                            if (brand == null) {
                                brand = createBrandFrom(broadcast);
                            }
                            updateBrand(brand, broadcast);
                            addOrReplaceItemInPlaylist(item, brand);
                            writer.createOrUpdateDefinitivePlaylist(brand);
                        }
                    }
                }
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri).withSource(getClass()));
            }
        }

        private void addOrReplaceItemInPlaylist(Item item, Playlist playlist) {
            int itemIndex = playlist.getItems().indexOf(item);
            if (itemIndex >= 0) {
                List<Item> items = Lists.newArrayList(playlist.getItems());
                items.set(itemIndex, item);
                playlist.setItems(items);
            } else {
                playlist.addItem(item);
            }
        }

        private void updateSeries(Series series, IonBroadcast broadcast) {
            series.setTitle(broadcast.getEpisode().getSeriesTitle());
        }

        private Series createSeries(IonBroadcast broadcast) {
            return new Series(SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId(), BBC_CURIE_BASE + broadcast.getSeriesId());
        }

        private Brand createBrandFrom(IonBroadcast broadcast) {
            return new Brand(SLASH_PROGRAMMES_ROOT + broadcast.getBrandId(), BBC_CURIE_BASE + broadcast.getBrandId(), Publisher.BBC);
        }

        private void updateBrand(Brand brand, IonBroadcast broadcast) {
            brand.setTitle(broadcast.getEpisode().getBrandTitle());
        }

        private Item createItemFrom(IonBroadcast broadcast) {
            IonEpisode ionEpisode = broadcast.getEpisode();
            Item item;
            if (!Strings.isNullOrEmpty(broadcast.getBrandId())) {
                item = new Episode(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE + ionEpisode.getId(), Publisher.BBC);
            } else {
                item = new Item(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE + ionEpisode.getId(), Publisher.BBC);
            }
            item.setAliases(BbcAliasCompiler.bbcAliasUrisFor(item.getCanonicalUri()));
            item.setIsLongForm(true);
            if (!Strings.isNullOrEmpty(broadcast.getMediaType())) {
                item.setMediaType(MediaType.valueOf(broadcast.getMediaType().toUpperCase()));
            }
            if (ionEpisode.getIsFilm() != null && ionEpisode.getIsFilm()) {
                item.setSpecialization(Specialization.FILM);
            }
            return item;
        }

        private void updateEpisodeDetails(Episode item, IonBroadcast broadcast) {
            // not sure how useful this is. there isn't mush else we can do.
            IonEpisode episode = broadcast.getEpisode();
            if (Strings.isNullOrEmpty(episode.getSubseriesId())) {
                item.setEpisodeNumber(episode.getPosition());
            }
        }

        private void updateItemDetails(Item item, IonBroadcast ionBroadcast) {

            Broadcast broadcast = atlasBroadcastFrom(ionBroadcast);

            Version broadcastVersion = getBroadcastVersion(item, ionBroadcast);
            if (broadcastVersion == null) {
                broadcastVersion = versionFrom(ionBroadcast);
                item.addVersion(broadcastVersion);
            }
            broadcastVersion.addBroadcast(broadcast);

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
            for (Version version : item.getVersions()) {
                if (version.getCanonicalUri().equals(SLASH_PROGRAMMES_ROOT + ionBroadcast.getVersionId())) {
                    return version;
                }
            }
            return null;
        }

        private Broadcast atlasBroadcastFrom(IonBroadcast ionBroadcast) {
            Broadcast broadcast = new Broadcast(BbcIonServices.get(ionBroadcast.getService()), ionBroadcast.getStart(), ionBroadcast.getEnd());
            broadcast.withId(BBC_CURIE_BASE + ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
            broadcast.setLastUpdated(ionBroadcast.getUpdated());
            return broadcast;
        }

    }

}
