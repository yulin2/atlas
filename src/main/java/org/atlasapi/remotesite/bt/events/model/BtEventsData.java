package org.atlasapi.remotesite.bt.events.model;

import org.atlasapi.remotesite.bt.events.feedModel.BtEvent;
import org.atlasapi.remotesite.bt.events.feedModel.BtTeam;
import org.atlasapi.remotesite.events.EventsData;

import com.google.common.collect.ImmutableList;


public class BtEventsData implements EventsData<BtTeam, BtEvent> {

    private final Iterable<BtEvent> matches;
    
    public BtEventsData(Iterable<BtEvent> matches) {
        this.matches = ImmutableList.copyOf(matches);
    }
    
    public Iterable<BtEvent> matches() {
        return matches;
    }

    @Override
    public Iterable<BtTeam> teams() {
        return ImmutableList.of();
    }
}
