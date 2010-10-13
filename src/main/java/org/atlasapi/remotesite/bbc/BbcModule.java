package org.atlasapi.remotesite.bbc;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.atoz.BbcSlashProgrammesAtoZUpdater;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;

@Configuration
public class BbcModule {

	private final static Daily BRAND_UPDATE_TIME = RepetitionRules.daily(new LocalTime(4, 0, 0));
	private final static Daily HIGHLIGHTS_UPDATE_TIME = RepetitionRules.daily(new LocalTime(10, 0, 0));
	
	private @Autowired ContentWriters contentWriters;
	private @Autowired AdapterLog log;
	private @Autowired SimpleScheduler scheduler;
	
	private @Value("${bbc.scheduledUpdates}") String enabled;
	
	@PostConstruct 
	public void scheduleTasks() {
		if (Boolean.parseBoolean(enabled)) {
			scheduler.schedule(bbcFeedsUpdater(), BRAND_UPDATE_TIME);
			scheduler.schedule(bbcHighlightsUpdater(), HIGHLIGHTS_UPDATE_TIME);
			log.record(new AdapterLogEntry(Severity.INFO)
				.withDescription("BBC update scheduled tasks installed"));
		} else {
			log.record(new AdapterLogEntry(Severity.INFO)
				.withDescription("Not installing BBC Scheduled tasks"));
		}
	}

	@Bean Runnable bbcHighlightsUpdater() {
		return new BbcIplayerHightlightsAdapter(contentWriters, log);
	}

	@Bean Runnable bbcFeedsUpdater() {
		return new BbcSlashProgrammesAtoZUpdater(contentWriters, log);
	}
	
	@Bean BbcProgrammeAdapter bbcProgrammeAdapter() {
		return new BbcProgrammeAdapter(log);
	}

	public Collection<SiteSpecificAdapter<? extends Content>> adapters() {
		return ImmutableList.<SiteSpecificAdapter<? extends Content>>of(bbcProgrammeAdapter());
	}
}
