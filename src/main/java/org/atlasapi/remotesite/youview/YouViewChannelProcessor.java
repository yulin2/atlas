package org.atlasapi.remotesite.youview;

import org.atlasapi.feeds.utils.UpdateProgress;
import org.atlasapi.media.channel.Channel;
import org.joda.time.DateTime;

import nu.xom.Elements;

public interface YouViewChannelProcessor {
    UpdateProgress process(Channel channel, Elements elements, DateTime startTime);
}
