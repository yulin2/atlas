package org.atlasapi.remotesite.bt.events;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class BtEventsUriCreatorTest {
    
    private final BtEventsUriCreator uriCreator = new BtEventsUriCreator();

    @Test
    public void testEventUriCreation() {
        String id = "1234";
        
        String eventUri = uriCreator.createEventUri(id);
        
        assertEquals("http://bt.com/events/1234", eventUri);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTeamUriCreation() {
        uriCreator.createTeamUri("1234");
    }
}
