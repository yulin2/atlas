package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.deserializerForClass;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.bbc.atoz.BbcSlashProgrammesAtoZUpdater;
import org.atlasapi.remotesite.bbc.ion.BbcIonDateRangeScheduleUpdater;
import org.atlasapi.remotesite.bbc.ion.BbcIonEpisodeDetailItemFetcherClient;
import org.atlasapi.remotesite.bbc.ion.BbcIonOndemandChangeUpdater;
import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleController;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.atlasapi.remotesite.bbc.schedule.BbcScheduleController;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BbcModule {

	private final static RepetitionRule BRAND_UPDATE_TIME = RepetitionRules.NEVER;
	private final static RepetitionRule HIGHLIGHTS_UPDATE_TIME = RepetitionRules.daily(new LocalTime(10, 0, 0));
	private final static RepetitionRule TEN_MINUTES = RepetitionRules.every(Duration.standardMinutes(10));
	private final static RepetitionRule SEVEN_MINUTES = RepetitionRules.every(Duration.standardMinutes(10));
	private final static RepetitionRule ONE_HOUR = RepetitionRules.every(Duration.standardHours(1));

    private @Autowired MongoDbBackedContentStore contentStore;
	private @Autowired ContentWriters contentWriters;
	private @Autowired AdapterLog log;
	private @Autowired SimpleScheduler scheduler;
	private @Autowired ItemsPeopleWriter itemsPeopleWriter;
	
    @PostConstruct
    public void scheduleTasks() {
        scheduler.schedule(bbcFeedsUpdater(), BRAND_UPDATE_TIME);
        scheduler.schedule(bbcHighlightsUpdater(), HIGHLIGHTS_UPDATE_TIME);
        
        scheduler.schedule(bbcIonUpdater(0, 0)
        		.withItemFetchClient(new BbcIonEpisodeDetailItemFetcherClient(log))
        		.withName("BBC Ion schedule update (today only)"), 
        		TEN_MINUTES);
        
        scheduler.schedule(bbcIonUpdater(7, 7)
        		.withItemFetchClient(new BbcIonEpisodeDetailItemFetcherClient(log))
        		.withName("BBC Ion schedule update (14 days)"),
        		ONE_HOUR);
        
        scheduler.schedule(bbcIonOndemandChangeUpdater(), SEVEN_MINUTES);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC update scheduled tasks installed"));
    }
	
	private BbcIonDateRangeScheduleUpdater bbcIonUpdater(int lookBack, int lookAhead) {
        return new BbcIonDateRangeScheduleUpdater(lookBack, lookAhead, contentStore, contentWriters, deserializerForClass(IonSchedule.class), itemsPeopleWriter, log);
    }
	
	@Bean BbcScheduleController bbcScheduleController() {
	    return new BbcScheduleController(contentStore, bbcProgrammeAdapter(), contentWriters, log);
	}
	
	@Bean BbcIonScheduleController bbcIonScheduleController() {
	    return new BbcIonScheduleController(contentStore, contentWriters, itemsPeopleWriter, log);
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
	
	@Bean BbcIonOndemandChangeUpdater bbcIonOndemandChangeUpdater() {
	    return new BbcIonOndemandChangeUpdater(contentStore, contentWriters, deserializerForClass(IonOndemandChanges.class), log);
	}
}
