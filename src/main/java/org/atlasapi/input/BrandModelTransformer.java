package org.atlasapi.input;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.Clock;

public class BrandModelTransformer extends ContentModelTransformer<org.atlasapi.media.entity.simple.Playlist, Brand> {

	public BrandModelTransformer(ContentResolver resolver,
			TopicStore topicStore, Clock clock) {
		super(resolver, topicStore, clock);
	}

	@Override
	protected Brand createContentOutput(Playlist input, DateTime now) {
		return new Brand();
	}

}
