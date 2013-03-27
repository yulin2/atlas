package org.atlasapi.remotesite.youview;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.remotesite.redux.UpdateProgress;
import org.joda.time.DateTime;

import nu.xom.Elements;

public interface YouViewChannelProcessor {
    UpdateProgress process(Channel channel, Elements elements, DateTime startTime);
}
