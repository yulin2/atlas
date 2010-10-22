package org.atlasapi.remotesite.health;

import java.util.List;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.mongo.MongoRoughSearch;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.webapp.health.HealthProbe;
import com.metabroadcast.common.webapp.health.ProbeResult;

public class BroadcasterProbe implements HealthProbe {

	private final Clock clock = new SystemClock();
	private final Duration maxStaleness = Duration.standardHours(30);
	
	private final Publisher publisher;
	private final MongoRoughSearch contentStore;
	private final Iterable<String> uris;
	
	
	public BroadcasterProbe(Publisher publisher, Iterable<String> uris, MongoRoughSearch contentStore) {
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
		 List<Playlist> playlists = contentStore.dehydratedPlaylistsMatching(new ContentQuery(Attributes.PLAYLIST_URI.createQuery(Operators.EQUALS, ImmutableList.of(letter))));
		 if (playlists.isEmpty()) {
			 return null;
		 }
		 return Iterables.getOnlyElement(playlists);
	}

	@Override
	public String title() {
		return publisher.title();
	}
}
