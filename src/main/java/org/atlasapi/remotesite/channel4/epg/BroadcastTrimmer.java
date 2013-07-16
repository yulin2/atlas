package org.atlasapi.remotesite.channel4.epg;

import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.joda.time.Interval;

public interface BroadcastTrimmer {

    void trimBroadcasts(Interval scheduleInterval, Channel channel, List<ItemRefAndBroadcast> acceptableIds);
    
}