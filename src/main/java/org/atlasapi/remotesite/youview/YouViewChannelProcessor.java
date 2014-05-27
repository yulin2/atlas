package org.atlasapi.remotesite.youview;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.Interval;

import com.metabroadcast.common.scheduling.UpdateProgress;

import nu.xom.Elements;

public interface YouViewChannelProcessor {
    UpdateProgress process(Channel channel, Publisher targetPublisher, Elements elements, Interval schedulePeriod);
}
