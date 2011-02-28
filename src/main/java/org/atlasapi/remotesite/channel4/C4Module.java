package org.atlasapi.remotesite.channel4;

import static org.atlasapi.media.entity.Publisher.C4;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import nu.xom.Builder;
import nu.xom.Document;

import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
import org.atlasapi.remotesite.channel4.epg.C4EpgElementFactory;
import org.atlasapi.remotesite.channel4.epg.C4EpgUpdater;
import org.atlasapi.remotesite.support.atom.AtomClient;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.sun.syndication.feed.atom.Feed;

@Configuration
public class C4Module {

	private final static Daily BRAND_UPDATE_TIME = RepetitionRules.daily(new LocalTime(2, 0, 0));
	private final static Daily HIGHLIGHTS_UPDATE_TIME = RepetitionRules.daily(new LocalTime(10, 0, 0));
	private final static RepetitionRule TWO_HOURS = RepetitionRules.every(Duration.standardHours(2));

	private @Autowired SimpleScheduler scheduler;
	private @Value("${c4.apiKey}") String c4ApiKey;

	private @Autowired MongoDbBackedContentStore contentStore;
	private @Autowired ContentWriters contentWriter;
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
        scheduler.schedule(c4HighlightsUpdater(), HIGHLIGHTS_UPDATE_TIME);
        log.record(new AdapterLogEntry(Severity.INFO).withDescription("C4 update scheduled tasks installed").withSource(getClass()));
    }

	@Bean public C4EpgUpdater c4EpgUpdater() {
	    BroadcastTrimmer trimmer = new BroadcastTrimmer(C4, scheduleResolver, contentWriter, log);
        return new C4EpgUpdater(c4EpgAtomClient(), contentWriter, contentStore, trimmer, log);
    }
	
	@Bean public RemoteSiteClient<Document> c4EpgAtomClient() {
	    SimpleHttpClient httpClient = new SimpleHttpClientBuilder()
            .withUserAgent(HttpClients.ATLAS_USER_AGENT)
            .withSocketTimeout(30, TimeUnit.SECONDS)
            .withRetries(3)
        .build();
        return new RequestLimitingRemoteSiteClient<Document>(new ApiKeyAwareClient<Document>(c4ApiKey, new XmlClient(httpClient, new Builder(new C4EpgElementFactory()))), 2);
	}

    @Bean C4AtoZAtomContentLoader c4AtozUpdater() {
		return new C4AtoZAtomContentLoader(c4AtomFetcher(), c4BrandFetcher(), contentWriter, contentStore, log);
	}
	
	@Bean C4HighlightsAdapter c4HighlightsUpdater() {
		return new C4HighlightsAdapter(contentWriter, log);
	}
	
	protected @Bean RemoteSiteClient<Feed> c4AtomFetcher() {
	    return new RequestLimitingRemoteSiteClient<Feed>(new ApiKeyAwareClient<Feed>(c4ApiKey, new AtomClient()), 4);
	}

	protected @Bean C4AtomBackedBrandAdapter c4BrandFetcher() {
		return new C4AtomBackedBrandAdapter(c4AtomFetcher(), contentStore, contentStore, log);
	}
}
