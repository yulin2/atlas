package org.atlasapi.remotesite.channel4;

import static org.atlasapi.media.entity.Publisher.C4;

import javax.annotation.PostConstruct;

import nu.xom.Builder;
import nu.xom.Document;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.channel4.epg.C4EpgBrandlessEntryProcessor;
import org.atlasapi.remotesite.channel4.epg.C4EpgElementFactory;
import org.atlasapi.remotesite.channel4.epg.C4EpgEntryProcessor;
import org.atlasapi.remotesite.channel4.epg.C4EpgUpdater;
import org.atlasapi.remotesite.channel4.epg.ScheduleResolverBroadcastTrimmer;
import org.atlasapi.remotesite.support.atom.AtomClient;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.time.DayRangeGenerator;
import com.sun.syndication.feed.atom.Feed;

@Configuration
public class C4Module {

	private final static Daily BRAND_UPDATE_TIME = RepetitionRules.daily(new LocalTime(2, 0, 0));
	private final static Daily HIGHLIGHTS_UPDATE_TIME = RepetitionRules.daily(new LocalTime(10, 0, 0));
	private final static RepetitionRule TWO_HOURS = RepetitionRules.every(Duration.standardHours(2));

	private @Autowired SimpleScheduler scheduler;
	private @Value("${c4.apiKey}") String c4ApiKey;

	private @Autowired ContentResolver contentResolver;
	private @Autowired ContentWriter contentWriter;
	private @Autowired AdapterLog log;
	private @Autowired ScheduleResolver scheduleResolver;
	
    @PostConstruct
    public void startBackgroundTasks() {
        if ("DISABLED".equals(c4ApiKey)) {
            log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("API key required for C4 updaters"));
            return;
        }
        scheduler.schedule(c4EpgUpdater(), TWO_HOURS);
        scheduler.schedule(c4AtozUpdater(), BRAND_UPDATE_TIME);
//        scheduler.schedule(c4HighlightsUpdater(), HIGHLIGHTS_UPDATE_TIME);
        log.record(new AdapterLogEntry(Severity.INFO).withDescription("C4 update scheduled tasks installed").withSource(getClass()));
    }

	@Bean public C4EpgUpdater c4EpgUpdater() {
	    ScheduleResolverBroadcastTrimmer trimmer = new ScheduleResolverBroadcastTrimmer(C4, scheduleResolver, contentResolver, lastUpdatedSettingContentWriter(), log);
        return new C4EpgUpdater(
                c4EpgAtomClient(), 
                new C4EpgEntryProcessor(lastUpdatedSettingContentWriter(), contentResolver, c4BrandFetcher(), log), 
                new C4EpgBrandlessEntryProcessor(lastUpdatedSettingContentWriter(), contentResolver, c4BrandFetcher(), log), 
                trimmer,
                log,
                new DayRangeGenerator().withLookAhead(7).withLookBack(7));
    }
	
	@Bean public RemoteSiteClient<Document> c4EpgAtomClient() {
        return new ApiKeyAwareClient<Document>(c4ApiKey, new XmlClient(requestLimitedHttpClient(), new Builder(new C4EpgElementFactory())));
	}

    @Bean C4AtoZAtomContentLoader c4AtozUpdater() {
		return new C4AtoZAtomContentLoader(c4AtomFetcher(), c4BrandFetcher(), log);
	}

	protected @Bean RemoteSiteClient<Feed> c4AtomFetcher() {
	    return new ApiKeyAwareClient<Feed>(c4ApiKey, new AtomClient(requestLimitedHttpClient()));
	}
	
	protected @Bean SimpleHttpClient requestLimitedHttpClient() {
	    return new RequestLimitingSimpleHttpClient(HttpClients.webserviceClient(), 4);
	}

	protected /*@Bean*/ C4AtomBackedBrandUpdater c4BrandFetcher() {
		return new C4AtomBackedBrandUpdater(c4AtomFetcher(), contentResolver, lastUpdatedSettingContentWriter(), log);
	}
	
	@Bean public ContentWriter lastUpdatedSettingContentWriter() {
		return new LastUpdatedSettingContentWriter(contentResolver, new LastUpdatedCheckingContentWriter(log, contentWriter));
	}
	
}
