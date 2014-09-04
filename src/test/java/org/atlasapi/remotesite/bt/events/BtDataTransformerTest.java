package org.atlasapi.remotesite.bt.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.atlasapi.remotesite.bt.events.model.BtEventsData;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.io.Resources;


public class BtDataTransformerTest {

    private final BtEventsDataTransformer transformer = new BtEventsDataTransformer();
    
    @Test
    public void testConversionToBtDataFormat() throws IOException {
        InputStream stream = streamFromFile("moto_gp_feed.json");
        BtEventsData data = transformer.transform(stream);
        
        assertTrue(Iterables.isEmpty(data.teams()));
        
        assertEquals(19, Iterables.size(data.matches()));
    }
    
    private InputStream streamFromFile(String filename) throws IOException {
        URL testFile = Resources.getResource(getClass(), filename);
        return testFile.openStream();
    }

}
