package org.atlasapi.remotesite.youview;

public class YouViewTodayUpdater extends YouViewUpdater {

    public YouViewTodayUpdater(YouViewChannelResolver channelResolver, YouViewScheduleFetcher fetcher, YouViewChannelProcessor processor, YouViewIngestConfiguration ingestConfiguration) {
        super(channelResolver, fetcher, processor, ingestConfiguration, 0, 0);
    }

}
