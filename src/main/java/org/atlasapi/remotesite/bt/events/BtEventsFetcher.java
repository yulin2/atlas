package org.atlasapi.remotesite.bt.events;

import org.atlasapi.remotesite.bt.events.feedModel.BtEvent;
import org.atlasapi.remotesite.bt.events.feedModel.BtTeam;
import org.atlasapi.remotesite.bt.events.model.BtSportType;
import org.atlasapi.remotesite.events.EventsFetcher;


public interface BtEventsFetcher extends EventsFetcher<BtSportType, BtTeam, BtEvent> {

}
