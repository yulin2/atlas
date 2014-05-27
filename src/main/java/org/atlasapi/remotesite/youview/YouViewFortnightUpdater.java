package org.atlasapi.remotesite.youview;


public class YouViewFortnightUpdater extends YouViewUpdater {

    public YouViewFortnightUpdater(YouViewChannelResolver channelResolver, YouViewScheduleFetcher fetcher, YouViewChannelProcessor processor, YouViewIngestConfiguration ingestConfiguration) {
        super(channelResolver, fetcher, processor, ingestConfiguration, 7, 7);
    }

}
