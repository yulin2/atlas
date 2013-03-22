package org.atlasapi.remotesite.youview;

import nu.xom.Elements;

import org.atlasapi.feeds.utils.UpdateProgress;
import org.atlasapi.media.channel.Channel;
import org.joda.time.DateTime;

public interface YouViewChannelProcessor {
    UpdateProgress process(Channel channel, Elements elements, DateTime startTime);
}
