package org.atlasapi.input;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.topic.TopicStore;
import org.joda.time.DateTime;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.time.Clock;

public class ClipModelTransformer extends ItemModelTransformer  {
    public ClipModelTransformer(LookupEntryStore lookupStore, TopicStore topicStore, NumberToShortStringCodec idCodec, Clock clock) {
        super(lookupStore, topicStore, idCodec, null, clock);
    }

    @Override
    protected Item createContentOutput(org.atlasapi.media.entity.simple.Item inputItem, DateTime now) {
        Item item = new Clip();
        item.setLastUpdated(now);
        return setItemFields(item, inputItem, now);
    }
}
