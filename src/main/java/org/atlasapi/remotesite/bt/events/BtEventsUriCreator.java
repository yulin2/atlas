package org.atlasapi.remotesite.bt.events;

import org.atlasapi.remotesite.events.EventsUriCreator;


public class BtEventsUriCreator implements EventsUriCreator {

    private static final String EVENT_URI_BASE = "http://bt.com/events/";
    
    public BtEventsUriCreator() { }

    @Override
    public String createEventUri(String id) {
        return EVENT_URI_BASE + id;
    }

    /**
     * BT currently don't provide Team information
     */
    @Override
    public String createTeamUri(String id) {
        throw new UnsupportedOperationException();
    }
}
