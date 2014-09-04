package org.atlasapi.remotesite.opta.events.soccer;

import static org.junit.Assert.assertEquals;

import org.atlasapi.remotesite.opta.events.OptaEventsUriCreator;
import org.junit.Test;


public class OptaUriCreatorTest {
    
    private final OptaEventsUriCreator uriCreator = new OptaEventsUriCreator();

    @Test
    public void testEventUriCreation() {
        String id = "1234";
        
        String eventUri = uriCreator.createEventUri(id);
        
        assertEquals("http://optasports.com/events/1234", eventUri);
    }

    @Test
    public void testTeamUriCreation() {
        String id = "1234";
        
        String teamUri = uriCreator.createTeamUri(id);
        
        assertEquals("http://optasports.com/teams/1234", teamUri);
    }
}
