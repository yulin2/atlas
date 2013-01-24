package org.atlasapi.remotesite.youview;

import org.joda.time.Duration;

public class YouViewFortnightUpdater extends YouViewUpdater {

    public YouViewFortnightUpdater(YouViewScheduleFetcher fetcher, YouViewXmlElementHandler elementHandler) {
        super(fetcher, elementHandler, Duration.standardDays(7), Duration.standardDays(7));
    }

}
