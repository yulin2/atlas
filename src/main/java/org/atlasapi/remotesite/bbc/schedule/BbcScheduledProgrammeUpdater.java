package org.atlasapi.remotesite.bbc.schedule;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.schedule.ChannelSchedule.Programme;

/**
 * Updater to download advance BBC schedules and get URIplay to load data for the programmes that they include
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcScheduledProgrammeUpdater implements Runnable {

	private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";

	private final RemoteSiteClient<ChannelSchedule> scheduleClient;
	private final Fetcher<Content> fetcher;

	private final Iterable<String> uris;

	private final AdapterLog log;
	
	public BbcScheduledProgrammeUpdater(Fetcher<Content> fetcher, Iterable<String> uris, AdapterLog log) throws JAXBException {
		this(new BbcScheduleClient(), fetcher, uris, log);
	}
	
	BbcScheduledProgrammeUpdater(RemoteSiteClient<ChannelSchedule> scheduleClient, Fetcher<Content> fetcher, Iterable<String> uris, AdapterLog log) {
		this.scheduleClient = scheduleClient;
		this.fetcher = fetcher;
		this.uris = uris;
		this.log = log;
	}

	private void update(String uri) {
		try {
			ChannelSchedule schedule = scheduleClient.get(uri);
			List<Programme> programmes = schedule.programmes();
			for (Programme programme : programmes) {
				if (programme.isEpisode()) {
					fetcher.fetch(SLASH_PROGRAMMES_BASE_URI + programme.pid());
				}
			}
		} catch (Exception e) {
			log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withDescription("Exception updating BBC URI " + uri));
		}
		
	}

	@Override
	public void run() {
		log.record(new AdapterLogEntry(Severity.INFO).withDescription("BBC Schedule Updater started"));
		for (String uri : uris) {
			log.record(new AdapterLogEntry(Severity.DEBUG).withDescription("Updating from schedule: " + uri));
			update(uri);
		}
		log.record(new AdapterLogEntry(Severity.INFO).withDescription("BBC Schedule Updater finished"));
	}

}
