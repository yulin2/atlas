package org.atlasapi.remotesite.youview;

public class YouViewTodayUpdater extends YouViewUpdater {

    public YouViewTodayUpdater(YouViewChannelResolver channelResolver, YouViewScheduleFetcher fetcher, YouViewChannelProcessor processor) {
        super(channelResolver, fetcher, processor, 0, 0);
    }

}
