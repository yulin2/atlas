package org.atlasapi.input;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.topic.TopicStore;
import org.joda.time.DateTime;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.time.Clock;

public class BrandModelTransformer extends ContentModelTransformer<org.atlasapi.media.entity.simple.Playlist, Brand> {

	public BrandModelTransformer(LookupEntryStore lookupStore, 
			TopicStore topicStore, NumberToShortStringCodec idCodec, 
			ClipModelTransformer clipsModelTransformer, Clock clock) {
		super(lookupStore, topicStore, idCodec, clipsModelTransformer, clock);
	}

	@Override
	protected Brand createContentOutput(Playlist input, DateTime now) {
		return new Brand();
	}

}
