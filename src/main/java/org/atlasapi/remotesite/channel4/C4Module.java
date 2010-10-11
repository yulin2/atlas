package org.atlasapi.remotesite.channel4;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.support.atom.AtomClient;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
import com.sun.syndication.feed.atom.Feed;

@Configuration
public class C4Module {

	private final static Daily BRAND_UPDATE_TIME = RepetitionRules.daily(new LocalTime(5, 0, 0));
	private final static Daily HIGHLIGHTS_UPDATE_TIME = RepetitionRules.daily(new LocalTime(10, 0, 0));
	
	private final SimpleScheduler scheduler = new SimpleScheduler();
	
	private @Value("${c4.apiKey}") String c4ApiKey;

	private @Autowired MongoDbBackedContentStore contentStore;
	private @Autowired ContentWriters contentWriter;
	private @Autowired AdapterLog log;
	
	@PostConstruct
	public void startBackgroundTasks(){
		if (!"DISABLED".equals(c4ApiKey)) {
			scheduler.schedule(c4AtozUpdater(), BRAND_UPDATE_TIME);			;
			scheduler.schedule(c4HighlightsUpdater(), HIGHLIGHTS_UPDATE_TIME);			;
			log.record(new AdapterLogEntry(Severity.INFO)
				.withDescription("C4 update scheduled tasks installed")
				.withSource(C4AtoZAtomContentLoader.class));
		} else {
			log.record(new AdapterLogEntry(Severity.INFO)
				.withDescription("Not installing C4 Adapters because API Key not present")
				.withSource(C4AtoZAtomContentLoader.class));
		}
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
		return new C4AtomBackedBrandAdapter(c4AtomFetcher(), contentStore, log);
	}

	public Collection<SiteSpecificAdapter<? extends Content>> adapters() {
		if (!"DISABLED".equals(c4ApiKey)) {
			return ImmutableList.<SiteSpecificAdapter<? extends Content>>of(c4BrandFetcher());
		} 
		return ImmutableList.of();
	}
}
