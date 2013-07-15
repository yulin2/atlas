package org.atlasapi.remotesite.youtube;

import java.util.List;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentWriters;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;

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
    @Autowired ContentResolver resolver;
	
	@PostConstruct
	public void startFeedUpdater() {
		scheduler.schedule(new YouTubeFeedUpdater(FEED_URLS, log, writers, resolver), RepetitionRules.every(Duration.standardMinutes(30)));
	}
}
