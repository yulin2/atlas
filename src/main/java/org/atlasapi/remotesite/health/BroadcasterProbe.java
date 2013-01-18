package org.atlasapi.remotesite.health;

import java.util.Map.Entry;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Strings;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class BroadcasterProbe implements HealthProbe {

	private final Clock clock = new SystemClock();
	private final Duration maxStaleness = Duration.standardHours(30);
	
	private final Publisher publisher;
	private final ContentResolver contentStore;
	private final Iterable<String> uris;
	
	
	public BroadcasterProbe(Publisher publisher, Iterable<String> uris, ContentResolver contentStore) {
		this.publisher = publisher;
		this.uris = uris;
		this.contentStore = contentStore;
	}
	
	@Override
	public ProbeResult probe() {
		ProbeResult result = new ProbeResult(title());
		for (Entry<Id, Maybe<Identified>> uriContent : contentStore.findByCanonicalUris(uris).asMap().entrySet()) {
		    Maybe<Identified> content = uriContent.getValue();
			if (content.hasValue() && content.requireValue() instanceof Described) {
			    Described playlist = (Described) content.requireValue();
				result.add(Strings.isNullOrEmpty(playlist.getTitle())?playlist.getCanonicalUri():playlist.getTitle(), playlist.getLastFetched().toString(DateTimeFormat.mediumDateTime()), playlist.getLastFetched().isAfter(clock.now().minus(maxStaleness)));
			} else {
				result.addFailure(uriContent.getKey().toString(), "not found");
			}
		}
		return result;
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
