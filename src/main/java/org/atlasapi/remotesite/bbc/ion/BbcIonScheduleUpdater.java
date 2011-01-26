package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.Duration;

import com.google.common.base.Strings;
import com.metabroadcast.common.http.SimpleHttpClient;


public class BbcIonScheduleUpdater implements Runnable {

    private final Iterable<String> uriSource;
    private final ContentResolver localFetcher;
    private final AdapterLog log;
    
    private final SimpleHttpClient httpClient;
    private final DefinitiveContentWriter writer;

    public BbcIonScheduleUpdater(Iterable<String> uriSource, ContentResolver localFetcher, DefinitiveContentWriter writer, AdapterLog log) {
        this.uriSource = uriSource;
        this.localFetcher = localFetcher;
        this.writer = writer;
        this.log = log;
        httpClient = HttpClients.webserviceClient();
    }
    
    @Override
    public void run() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update initiated"));

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (String uri : uriSource) {
            executor.submit(new BbcIonScheduleUpdateTask(uri));
        }
        executor.shutdown();
        boolean completion = false;
        try {
            completion = executor.awaitTermination(30, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("BBC Ion Schedule Update interrupted waiting for completion").withCause(e));
        }
        
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Update finished (" + (completion ? "normally)" : "timed-out)")));
    }

    private class BbcIonScheduleUpdateTask implements Runnable {

        private static final String BBC_CURIE_BASE = "bbc:";
        private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
        private final String uri;

        public BbcIonScheduleUpdateTask(String uri) {
            this.uri = uri;
        }

        @Override
        public void run() {
            try {
                IonSchedule schedule = BbcIonScheduleDeserialiser.deserialise(httpClient.getContentsOf(uri));
                for (IonBroadcast broadcast : schedule.getBlocklist()) {
                    Item item = (Item) localFetcher.findByUri(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId());
                    if(item != null) {
                        updateItemDetails(item, broadcast);
                        if(item instanceof Episode) {
                            updateEpisodeDetails((Episode)item, broadcast);
                        }
                    } else {
                        item = createItemFrom(broadcast);
                    }
                    //here item is not null;
                    if(Strings.isNullOrEmpty(broadcast.getBrandId())) {
                        writer.createOrUpdateItem(item);
                    } else {
                        Brand brand = (Brand) localFetcher.findByUri(SLASH_PROGRAMMES_ROOT + broadcast.getBrandId());
                        if(brand != null) {
                            //update brand details
                        } else {
                            //brand = createBrandFrom(brodcast);
                        }
                        //ensure item in brand,
                        //write brand
                    }
                }
            } catch(Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri).withSource(getClass()));
            }
        }
        
        private Item createItemFrom(IonBroadcast broadcast) {
            IonEpisode ionEpisode = broadcast.getEpisode();
            Item item;
            if(!Strings.isNullOrEmpty(broadcast.getBrandId())) {
                Episode episode = new Episode(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE+ionEpisode.getId(), Publisher.BBC);
//                episode.setEpisodeNumber(position);
                item = episode;
            } else {
                item = new Item(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE+ionEpisode.getId(), Publisher.BBC);
            }
            if(!Strings.isNullOrEmpty(broadcast.getMediaType())) {
                item.setMediaType(MediaType.valueOf(broadcast.getMediaType().toUpperCase()));
            }
            if(ionEpisode.getIsFilm() != null && ionEpisode.getIsFilm()) {
                item.setSpecialization(Specialization.FILM);
            }
            updateItemDetails(item, broadcast);
            return item;
        }

        private void updateEpisodeDetails(Episode item, IonBroadcast broadcast) {
//            not sure how useful this is. there isn't mush else we can do.
            IonEpisode episode = broadcast.getEpisode();
            if(Strings.isNullOrEmpty(episode.getSubseriesId())) {
                item.setEpisodeNumber(episode.getPosition());
            }
        }

        private void updateItemDetails(Item item, IonBroadcast ionBroadcast) {
            
            Broadcast broadcast = atlasBroadcastFrom(ionBroadcast);
            
            Version broadcastVersion = getBroadcastVersion(item, ionBroadcast);
            if(broadcastVersion == null) {
            } else {
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
            
            version.setCanonicalUri(SLASH_PROGRAMMES_ROOT+ionBroadcast.getVersionId());
            version.setDuration(Duration.standardSeconds(ionBroadcast.getDuration()));
            version.setProvider(Publisher.BBC);
            
            return version;
        }

        private Version getBroadcastVersion(Item item, IonBroadcast ionBroadcast) {
            for (Version version : item.getVersions()) {
                if(version.getCanonicalUri().equals(SLASH_PROGRAMMES_ROOT+ionBroadcast.getVersionId())) {
                    return version;
                }
            }
            return null;
        }

        private Broadcast atlasBroadcastFrom(IonBroadcast ionBroadcast) {
            Broadcast broadcast = new Broadcast(BbcIonServiceMap.get(ionBroadcast.getService()), ionBroadcast.getStart(), ionBroadcast.getEnd());
            broadcast.withId(BBC_CURIE_BASE+ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
            broadcast.setLastUpdated(ionBroadcast.getUpdated());
            return broadcast;
        }

    }
    
}
