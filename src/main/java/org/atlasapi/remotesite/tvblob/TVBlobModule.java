package org.atlasapi.remotesite.tvblob;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class TVBlobModule {

	private static final LocalTime ONE_AM = new LocalTime(1, 0, 0);
	private static final LocalTime TWO_AM = new LocalTime(2, 0, 0);
	
	private @Autowired SimpleScheduler scheduler;
	private @Autowired ContentWriter writer;
	private @Autowired ContentResolver contentResolver;
	
	@PostConstruct
	public void startBackgroundUpdater() {
		scheduler.schedule(updateToday(), RepetitionRules.daily(ONE_AM));
		scheduler.schedule(updateTomorrow(), RepetitionRules.daily(TWO_AM));
	}
	
	@Bean TVBlobServicesUpdater updateToday() {
		return new TVBlobServicesUpdater(writer, contentResolver, "today");
	}
	
	@Bean TVBlobServicesUpdater updateTomorrow() {
		return new TVBlobServicesUpdater(writer, contentResolver, "tomorrow");
	}
}
