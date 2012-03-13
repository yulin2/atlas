package org.atlasapi.remotesite.pa.cassandra;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.ScheduleEntry;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;

/**
 */
public class DummyScheduleWriter implements ScheduleWriter{

    @Override
    public void writeScheduleFor(Iterable<? extends Item> items) {
    }

    @Override
    public void writeCompleteEntry(ScheduleEntry entry) {
    }

    @Override
    public void replaceScheduleBlock(Publisher publisher, Channel channel, Iterable<ItemRefAndBroadcast> itemsAndBroadcasts) {
    }
    
}
