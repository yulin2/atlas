package org.atlasapi.remotesite.bbc;

import static com.metabroadcast.common.scheduling.RepetitionRules.every;
import static com.metabroadcast.common.time.DateTimeZones.UTC;
import static org.atlasapi.remotesite.bbc.ion.BbcIonFeedClient.clientForFeedType;
import static org.joda.time.Duration.standardMinutes;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.topic.TopicStore;
import org.atlasapi.remotesite.bbc.atoz.BbcSlashProgrammesAtoZUpdater;
import org.atlasapi.remotesite.bbc.ion.BbcIonContainerFetcherClient;
import org.atlasapi.remotesite.bbc.ion.BbcIonDayRangeUrlSupplier;
import org.atlasapi.remotesite.bbc.ion.BbcIonEpisodeDetailItemFetcherClient;
import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleController;
import org.atlasapi.remotesite.bbc.ion.BbcIonScheduleUpdater;
import org.atlasapi.remotesite.bbc.ion.DefaultBbcIonScheduleHandler;
import org.atlasapi.remotesite.bbc.ion.OndemandBbcIonScheduleHandler;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.atlasapi.remotesite.bbc.ion.ondemand.BbcIonOndemandChangeTaskBuilder;
import org.atlasapi.remotesite.bbc.ion.ondemand.BbcIonOndemandChangeUpdateBuilder;
import org.atlasapi.remotesite.bbc.ion.ondemand.BbcIonOndemandChangeUpdateController;
import org.atlasapi.remotesite.bbc.ion.ondemand.BbcIonOndemandChangeUpdater;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.time.DayRangeGenerator;

@Configuration
public class BbcModule {

	private final static RepetitionRule BRAND_UPDATE_TIME = RepetitionRules.NEVER;
	private final static RepetitionRule TEN_MINUTES = RepetitionRules.every(Duration.standardMinutes(10));
	private final static RepetitionRule SEVEN_MINUTES = every(standardMinutes(10));
	private final static RepetitionRule ONE_HOUR = RepetitionRules.every(Duration.standardHours(1));
	
	public final static String SCHEDULE_ONDEMAND_FORMAT = "http://www.bbc.co.uk/iplayer/ion/schedule/service/%s/date/%s/media_set/pc/format/json";
	public final static String SCHEDULE_DEFAULT_FORMAT = "http://www.bbc.co.uk/iplayer/ion/schedule/service/%s/date/%s/timeslot/day/format/json";

    private @Autowired ContentResolver contentResolver;
	private @Autowired ContentWriter contentWriters;
	private @Autowired TopicStore topicStore;
	private @Autowired AdapterLog log;
	private @Autowired SimpleScheduler scheduler;
	private @Autowired ItemsPeopleWriter itemsPeopleWriter;
	private @Autowired DatabasedMongo mongo;
	
    @PostConstruct
    public void scheduleTasks() {
        scheduler.schedule(bbcFeedsUpdater(), BRAND_UPDATE_TIME);
        
        scheduler.schedule(bbcIonScheduleUpdater(0, 0).withName("BBC Ion schedule update (today only)"), TEN_MINUTES);
        scheduler.schedule(bbcIonScheduleUpdater(7, 7).withName("BBC Ion schedule update (14 days)"), ONE_HOUR);
        scheduler.schedule(bbcIonScheduleOndemandUpdater(7).withName("BBC Ion on-demand schedule update (7 days)"), every(standardMinutes(10)).withOffset(standardMinutes(5)));
        
        scheduler.schedule(bbcIonOndemandChangeUpdater().withName("BBC Ion Ondemand Change Updater"), SEVEN_MINUTES);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC update scheduled tasks installed"));
    }
	
    private BbcIonScheduleUpdater bbcIonScheduleUpdater(int lookBack, int lookAhead) {
        BbcIonDayRangeUrlSupplier urlSupplier = dayRangeUrlSupplier(SCHEDULE_DEFAULT_FORMAT, lookAhead, lookBack);
        return new BbcIonScheduleUpdater(urlSupplier, clientForFeedType(IonSchedule.class), defaultBbcIonScheduleHandler(), log);
    }
    
    private BbcIonScheduleUpdater bbcIonScheduleOndemandUpdater(int lookBack) {
        BbcIonDayRangeUrlSupplier urlSupplier = dayRangeUrlSupplier(SCHEDULE_ONDEMAND_FORMAT, 0, lookBack);
        return new BbcIonScheduleUpdater(urlSupplier, clientForFeedType(IonSchedule.class), new OndemandBbcIonScheduleHandler(contentResolver, contentWriters, log), log);
    }
	
	private BbcIonDayRangeUrlSupplier dayRangeUrlSupplier(String urlPattern, int ahead, int back) {
	    return new BbcIonDayRangeUrlSupplier(urlPattern, new DayRangeGenerator(UTC).withLookAhead(ahead).withLookBack(back));
	}
	
	@Bean BbcIonScheduleController bbcIonScheduleController() {
	    return new BbcIonScheduleController(clientForFeedType(IonSchedule.class), defaultBbcIonScheduleHandler(), log);
	}
	
    @Bean DefaultBbcIonScheduleHandler defaultBbcIonScheduleHandler() {
        return new DefaultBbcIonScheduleHandler(contentResolver, contentWriters, log)
            .withItemFetcherClient(new BbcIonEpisodeDetailItemFetcherClient(log))
            .withContainerFetcherClient(new BbcIonContainerFetcherClient(log))
            .withItemPeopleWriter(itemsPeopleWriter);
    }
    
    @Bean OndemandBbcIonScheduleHandler ondemandBbcIonScheduleHandler() {
        return null;
    }

	@Bean Runnable bbcFeedsUpdater() {
		return new BbcSlashProgrammesAtoZUpdater(contentWriters,  new ProgressStore(mongo), topicStore, log);
	}
	
	@Bean BbcSlashProgrammesController bbcFeedsController() {
	    return new BbcSlashProgrammesController(contentWriters, topicStore, log);
	}
	
	@Bean BbcIonOndemandChangeUpdater bbcIonOndemandChangeUpdater() {
	    return new BbcIonOndemandChangeUpdater(bbcIonOndemandChangeUpdateBuilder(), log);
	}

    private BbcIonOndemandChangeUpdateBuilder bbcIonOndemandChangeUpdateBuilder() {
        return new BbcIonOndemandChangeUpdateBuilder(new BbcIonOndemandChangeTaskBuilder(contentResolver, contentWriters, log), log, clientForFeedType(IonOndemandChanges.class));
    }
	
	@Bean BbcIonOndemandChangeUpdateController bbcIonOndemandChangeController() {
	    return new BbcIonOndemandChangeUpdateController(bbcIonOndemandChangeUpdateBuilder());
	}
}
