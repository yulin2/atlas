package org.atlasapi.remotesite.youtube;

import java.util.List;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

public class YouTubeModule {
	
	private static final List<String> FEED_URLS = ImmutableList.<String>builder()
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/top_rated")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/recently_featured")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/watch_on_mobile")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/most_discussed")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/top_favorites")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/most_responded")
		.add("http://gdata.youtube.com/feeds/api/standardfeeds/most_recent").build();

	@Autowired SimpleScheduler scheduler;
	@Autowired AdapterLog log;
	@Autowired ContentWriters writers;
	
	@PostConstruct
	public void startFeedUpdater() {
		scheduler.schedule(new YouTubeFeedUpdater(FEED_URLS, log, writers), RepetitionRules.atInterval(Duration.standardMinutes(30)));
	}
	
	public List<SiteSpecificAdapter<? extends Identified>> adapters() {
		return ImmutableList.<SiteSpecificAdapter<? extends Identified>>of(new YouTubeAdapter(), youTubeFeedAdapter());
		// Not included due to high load
		//new YouTubeUserAdapter();
	}

	@Bean YouTubeFeedAdapter youTubeFeedAdapter() {
		return new YouTubeFeedAdapter();
	}
}
