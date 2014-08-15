package org.atlasapi.remotesite.btvod;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;


public class BtPortalImageUriRetrieverTest {

    private static final String BASE_URI = "http://example.org/";
    private static final String PRODUCT_ID_WITH_IMAGE = "1";
    private static final String PRODUCT_ID_WITHOUT_IMAGE = "2";
    
    private String fileContentsFromResource(String resourceName)  {
        try {
            return Files.toString(new File(Resources.getResource(getClass(), resourceName).getFile()), Charsets.UTF_8);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return null;
    }
    
    private final SimpleHttpClient httpClient = new FixedResponseHttpClient(
            ImmutableMap.of(BASE_URI + "xml/product/" + PRODUCT_ID_WITH_IMAGE + ".xml", 
                            fileContentsFromResource("bt-vod-example.xml"),
                            BASE_URI + "xml/product/" + PRODUCT_ID_WITHOUT_IMAGE + ".xml",
                            fileContentsFromResource("bt-vod-example-no-image.xml")));
    
    private final BtPortalImageUriProvider uriRetriever = new BtPortalImageUriProvider(httpClient, BASE_URI);
    
    @Test
    public void testRetrievesImage() {
        assertThat(uriRetriever.imageUriFor(PRODUCT_ID_WITH_IMAGE).get(), is(BASE_URI + "b.png"));
    }
    
    @Test
    public void testReturnsAbsentIfAttributeMissing() {
        assertFalse(uriRetriever.imageUriFor(PRODUCT_ID_WITHOUT_IMAGE).isPresent());
    }
    
}
