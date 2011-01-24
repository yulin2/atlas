package org.atlasapi.remotesite.health;

import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Strings;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class BroadcasterProbe implements HealthProbe {

	private final Clock clock = new SystemClock();
	private final Duration maxStaleness = Duration.standardHours(30);
	
	private final Publisher publisher;
	private final MongoDbBackedContentStore contentStore;
	private final Iterable<String> uris;
	
	
	public BroadcasterProbe(Publisher publisher, Iterable<String> uris, MongoDbBackedContentStore contentStore) {
		this.publisher = publisher;
		this.uris = uris;
		this.contentStore = contentStore;
	}
	
	@Override
	public ProbeResult probe() {
		ProbeResult result = new ProbeResult(title());
		for (String uri : uris) {
			Playlist playlist = queryForPlaylist(uri);
			if (playlist != null) {
				result.add(Strings.isNullOrEmpty(playlist.getTitle())?uri:playlist.getTitle(), playlist.getLastFetched().toString(DateTimeFormat.mediumDateTime()), playlist.getLastFetched().isAfter(clock.now().minus(maxStaleness)));
			} else {
				result.addFailure(uri, "not found");
			}
		}
		return result;
	}

	private Playlist queryForPlaylist(String letter) {
		 return (Playlist) contentStore.findByUri(letter);
	}

	@Override
	public String title() {
		return publisher.title();
	}

    @Override
    public String slug() {
        return publisher.title().replaceAll(" ", "").toLowerCase();
    }
}
