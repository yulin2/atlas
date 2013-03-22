package org.atlasapi.remotesite.youview;

import org.joda.time.Duration;

public class YouViewFortnightUpdater extends YouViewUpdater {

    public YouViewFortnightUpdater(YouViewChannelResolver channelResolver, YouViewScheduleFetcher fetcher, YouViewChannelProcessor processor) {
        super(channelResolver, fetcher, processor, Duration.standardDays(7), Duration.standardDays(7));
    }

}
