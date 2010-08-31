package org.atlasapi.remotesite.tvblob;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.persistence.system.RemoteSiteClient;

public class TVBlobServicesClientTest extends TestCase {
    
    private RemoteSiteClient<List<TVBlobService>> client = new TVBlobServicesClient();
    
    public void testShouldRetrieveServices() throws Exception {
        List<TVBlobService> services = client.get("http://epgadmin.tvblob.com/api/services.json");
        assertFalse(services.isEmpty());
        
        
        for (TVBlobService service: services) {
            assertNotNull(service.getSlug());
            assertNotNull(service.getName());
        }
    }
}
