package org.atlasapi.remotesite.bbc.schedule;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBException;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule.Programme;

import com.google.common.collect.Iterables;

/**
 * Updater to download advance BBC schedules and get URIplay to load data for
 * the programmes that they include
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcScheduledProgrammeUpdater implements Runnable {

    private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";

	private final RemoteSiteClient<ChannelSchedule> scheduleClient;
    private final BbcProgrammeAdapter fetcher;

    private final Iterable<String> uris;

    private final AdapterLog log;

	private final ContentWriter writer;

    private final ContentResolver localFetcher;
	
    
    public BbcScheduledProgrammeUpdater(ContentResolver localFetcher, BbcProgrammeAdapter remoteFetcher, ContentWriter writer, Iterable<String> uris, AdapterLog log) throws JAXBException {
        this(new BbcScheduleClient(), localFetcher, remoteFetcher, writer, uris, log);
    }

    BbcScheduledProgrammeUpdater(RemoteSiteClient<ChannelSchedule> scheduleClient, ContentResolver localFetcher, BbcProgrammeAdapter remoteFetcher, ContentWriter writer,
            Iterable<String> uris, AdapterLog log) {
        this.scheduleClient = scheduleClient;
        this.localFetcher = localFetcher;
        this.fetcher = remoteFetcher;
        this.writer = writer;
        this.uris = uris;
        this.log = log;
    }


    private void update(String uri) {
        try {
            ChannelSchedule schedule = scheduleClient.get(uri);
            List<Programme> programmes = schedule.programmes();
            for (Programme programme : programmes) {
                try {
                    if (programme.isEpisode()) {
                        Item fetchedItem = (Item) fetcher.fetch(SLASH_PROGRAMMES_BASE_URI + programme.pid());
                        if (fetchedItem != null) {
                            writeFetchedItem(fetchedItem);
                        } else {
                            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Not processing programme with pid " + programme.pid()+ " it can't be retrieved by the fetcher"));
                        }
                    } else {
                        log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Not processing programme with pid " + programme.pid()+ " as it's not an episode"));
                    }
                } catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception updating programme with pid " + programme.pid()));
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception updating BBC URI " + uri));
        }
    }

    private void writeFetchedItem(Item fetchedItem) {
        if (fetchedItem instanceof Episode) {
            Episode fetchedEpisode = (Episode) fetchedItem;
            if (fetchedEpisode.getContainer() != null) {
                Container<Item> fetchedContainer = fetchContainerFor(fetchedEpisode.getContainer().getCanonicalUri());
                if (fetchedContainer != null) {
                    fetchedContainer.addOrReplace(fetchedEpisode);
                    writer.createOrUpdate(fetchedContainer);
                } else {
                    log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Can't write fetched Item "+fetchedItem+" it's Brand can't be fetched"));
                }
            } else {
                log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Can't write fetched Item "+fetchedItem+" as it has no container"));
            }
        } else {
            writer.createOrUpdate(fetchedItem);
        }
    }

    @SuppressWarnings("unchecked")
    private Container<Item> fetchContainerFor(String containerUri) {
        Container<Item> fetchedContainer = (Container<Item>) localFetcher.findByCanonicalUri(containerUri);
        if (fetchedContainer == null) {
            fetchedContainer = (Container<Item>) fetcher.fetch(containerUri);
        }
        return fetchedContainer;
    }

    @Override
    public void run() {
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Schedule Updater started"));

        final CountDownLatch done = new CountDownLatch(Iterables.size(uris));

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (final String uri : uris) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    log.record(new AdapterLogEntry(Severity.DEBUG).withSource(getClass()).withDescription("Updating from schedule: " + uri));
                    update(uri);
                    log.record(new AdapterLogEntry(Severity.DEBUG).withSource(getClass()).withDescription("Finished updating from schedule: " + uri));
                    done.countDown();
                }
            });
        }
        executor.shutdown();

        try {
            done.await();
            log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Schedule Updater finished"));
        } catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withCause(e).withDescription("BBC Schedule Updater interrupted waiting to finish"));
        }
    }
}
