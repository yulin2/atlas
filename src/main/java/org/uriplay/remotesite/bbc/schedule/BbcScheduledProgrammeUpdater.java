package org.uriplay.remotesite.bbc.schedule;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.bbc.schedule.ChannelSchedule.Programme;

/**
 * Updater to download advance BBC schedules and get URIplay to load data for the programmes that they include
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcScheduledProgrammeUpdater implements Runnable {

	private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";

	private static Log LOG = LogFactory.getLog(BbcScheduledProgrammeUpdater.class);
	
	private RemoteSiteClient<ChannelSchedule> scheduleClient;
	private Fetcher<Set<Object>> uriplayFetcher;

	private Iterable<String> uris;
	
	public BbcScheduledProgrammeUpdater() {
	}

	public BbcScheduledProgrammeUpdater(Fetcher<Set<Object>> uriplayFetcher) throws JAXBException {
		this(new BbcScheduleClient(), uriplayFetcher);
	}
	
	BbcScheduledProgrammeUpdater(RemoteSiteClient<ChannelSchedule> scheduleClient, Fetcher<Set<Object>> uriplayFetcher) {
		this.scheduleClient = scheduleClient;
		this.uriplayFetcher = uriplayFetcher;
	}

	private void update(String uri) {
		try {
			ChannelSchedule schedule = scheduleClient.get(uri);
			List<Programme> programmes = schedule.programmes();
			for (Programme programme : programmes) {
				if (programme.isEpisode()) {
					uriplayFetcher.fetch(SLASH_PROGRAMMES_BASE_URI + programme.pid(), null);
				}
			}
		} catch (Exception e) {
			LOG.warn((e));
		}
		
	}

	@Override
	public void run() {
		for (String uri : uris) {
			LOG.info("Updating from schedule: " + uri);
			update(uri);
		}
	}

	public void setUris(Iterable<String> uris) {
		this.uris = uris;
	}

}
