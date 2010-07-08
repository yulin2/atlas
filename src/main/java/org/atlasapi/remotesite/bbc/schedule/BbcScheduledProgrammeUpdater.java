package org.atlasapi.remotesite.bbc.schedule;

import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Content;
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

	private static Log LOG = LogFactory.getLog(BbcScheduledProgrammeUpdater.class);
	
	private RemoteSiteClient<ChannelSchedule> scheduleClient;
	private Fetcher<Content> fetcher;

	private Iterable<String> uris;
	
	public BbcScheduledProgrammeUpdater() {
	}

	public BbcScheduledProgrammeUpdater(Fetcher<Content> fetcher) throws JAXBException {
		this(new BbcScheduleClient(), fetcher);
	}
	
	BbcScheduledProgrammeUpdater(RemoteSiteClient<ChannelSchedule> scheduleClient, Fetcher<Content> fetcher) {
		this.scheduleClient = scheduleClient;
		this.fetcher = fetcher;
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
