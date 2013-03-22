package org.atlasapi.remotesite.youview;

import org.joda.time.Duration;

public class YouViewTodayUpdater extends YouViewUpdater {

    public YouViewTodayUpdater(YouViewChannelResolver channelResolver, YouViewScheduleFetcher fetcher, YouViewChannelProcessor processor) {
        super(channelResolver, fetcher, processor, Duration.standardDays(0), Duration.standardDays(1));
    }

}
