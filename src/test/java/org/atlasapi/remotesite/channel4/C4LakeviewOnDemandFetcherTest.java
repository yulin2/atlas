package org.atlasapi.remotesite.channel4;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.support.atom.AtomClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.sun.syndication.feed.atom.Feed;

@RunWith(MockitoJUnitRunner.class)
public class C4LakeviewOnDemandFetcherTest {
    
    private static final String API_ROOT = "http://api.channel4.com/pmlsd/";
    private static final String API_KEY = "api_key";
    private static final String BRAND = "blackout";
    private static final String P06_REQUEST_URI = API_ROOT + BRAND + "/4od.atom?platform=p06&apiKey=" + API_KEY;
    private static final String ITEM_URI = "http://www.channel4.com/programmes/" + BRAND + "/episode-guide/series-1/episode-1";
    
    private final SimpleHttpClient httpClient = new FixedResponseHttpClient(
            ImmutableMap.of(P06_REQUEST_URI, Files.toString(new File(Resources.getResource("blackout.xml").getFile()), Charsets.UTF_8)));
    private final RemoteSiteClient<Feed> atomClient = new AtomClient(httpClient);
    
    private final C4LakeviewOnDemandFetcher fetcher = new C4LakeviewOnDemandFetcher(atomClient, API_KEY, null);
    
    public C4LakeviewOnDemandFetcherTest() throws IOException {
        
    }
    
    @Test
    public void testExtractsOnDemandForBrand() {
        Location location = fetcher.lakeviewLocationFor(ComplexItemTestDataBuilder.complexItem().withUri(ITEM_URI).build());
        
        assertEquals("https://ais.channel4.com/asset/3573948", location.getUri());
        assertEquals(Platform.XBOX, location.getPolicy().getPlatform());
        
        // I don't think the availability start time is set correctly, as it's from TXDate not
        // availability start but I'm not touching it as Fred says it was done for a reason, 
        // not that he could remember why, nor is it documented.
        assertEquals(new DateTime(2013, DateTimeConstants.SEPTEMBER, 9, 22, 00, 0).withZone(DateTimeZone.forID("Europe/London")), 
                location.getPolicy().getAvailabilityStart());
        assertEquals(new DateTime(2013, DateTimeConstants.OCTOBER, 9, 22, 40, 0).withZone(DateTimeZone.forID("Europe/London")), 
                location.getPolicy().getAvailabilityEnd());
        
    }
    
}
