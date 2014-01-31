package org.atlasapi.remotesite.youview;

import org.atlasapi.media.channel.Channel;
import org.joda.time.Interval;

import com.metabroadcast.common.scheduling.UpdateProgress;

import nu.xom.Elements;

public interface YouViewChannelProcessor {
    UpdateProgress process(Channel channel, Elements elements, Interval schedulePeriod);
}
