package org.atlasapi.remotesite.btvod.portal;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;


public class XmlPortalClientTest {

    private static final String BASE_URI = "http://example.org/";
    
    private String fileContentsFromResource(String resourceName)  {
        try {
            return Files.toString(new File(Resources.getResource(getClass(), resourceName).getFile()), Charsets.UTF_8);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
    
    private final SimpleHttpClient httpClient = new FixedResponseHttpClient(
            ImmutableMap.of(BASE_URI + "test/all/1.xml",
                            fileContentsFromResource("1.xml"),
                            BASE_URI + "test/all/2.xml",
                            fileContentsFromResource("2.xml")));
    
    private final XmlPortalClient client = new XmlPortalClient(BASE_URI, httpClient);
    
    @Test
    public void testParseAndPaginate() throws HttpException, Exception {
        Set<String> productIdsForGroup = client.getProductIdsForGroup("test/all");
        assertTrue(productIdsForGroup.contains("C4_55809"));
    }
}
