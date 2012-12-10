package org.atlasapi.remotesite.youview;

import org.joda.time.Duration;

public class YouViewTodayUpdater extends YouViewUpdater {

    public YouViewTodayUpdater(YouViewScheduleFetcher fetcher, YouViewXmlElementHandler elementHandler) {
        super(fetcher, elementHandler, Duration.standardDays(0), Duration.standardDays(1));
    }

}
