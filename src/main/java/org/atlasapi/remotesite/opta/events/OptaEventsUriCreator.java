package org.atlasapi.remotesite.opta.events;

import org.atlasapi.remotesite.events.EventsUriCreator;


public class OptaEventsUriCreator implements EventsUriCreator {

    private static final String EVENT_URI_BASE = "http://optasports.com/events/";
    private static final String TEAM_URI_BASE = "http://optasports.com/teams/";
    
    public OptaEventsUriCreator() { }

    @Override
    public String createEventUri(String id) {
        return EVENT_URI_BASE + id;
    }

    @Override
    public String createTeamUri(String id) {
        return TEAM_URI_BASE + id;
    }
}
